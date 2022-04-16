/**
 * @program: Consumer
 * @description:
 * @author: Mr.Fang
 * @create: 2022-03-09 12:07
 **/
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

/**
 * consumer
 */
public class Consumer {
    private static final String QUEUE_NAME = "lifts_queue";
    private static final String IP_ADDRESS = "172.31.31.193";
    private static final boolean SERVICE1 = true;
    private static final boolean SERVICE2 = true;
    private static final int PORT = 5672;
    private static final ConnectionFactory factory = new ConnectionFactory();
    private static RabbitMQChannelPool rabbitMQChannelPool;
    /**
    * @Description: 
    * @Param: [args]
    * @return: void
    */
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        factory.setUsername("admin");
        factory.setPassword("root");
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(8);
        config.setMaxIdle(8);
        config.setMinIdle(0);
        RabbitMQChannelPoolFactory pool = new RabbitMQChannelPoolFactory(factory);
        rabbitMQChannelPool = new RabbitMQChannelPool(pool,config);
        Jedis jedis = new Jedis("localhost");


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Channel channel = rabbitMQChannelPool.getChannel();
                    channel.basicQos(16);

                    channel.basicConsume(QUEUE_NAME, false, new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body) throws IOException {
                            ArrayList<String> oldValue = new ArrayList<String>();
                            ArrayList<String> newValue = new ArrayList<String>();
                            String record = new String(body);
                            String[] recordList = record.split("/");
                            //add data to redis
                            if(SERVICE1) {
                                String day = recordList[5];
                                String skier = recordList[7];
                                String lift = recordList[11];
                                jedis.select(0);
                                jedis.lpush(skier, day + "_" + lift);
                            }
                            if(SERVICE2){
                                String resort = recordList[1];
                                String day = recordList[5];
                                String skier = recordList[7];
                                String time = recordList[9];
                                String lift = recordList[11];
                                jedis.select(1);
                                jedis.lpush(resort + "_" + day, skier + "_" + lift + "_" + time);
                            }
                            try {
                                Thread.sleep(3);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            channel.basicAck(envelope.getDeliveryTag(), false);

                        }
                    });
                    rabbitMQChannelPool.returnChannel(channel);
                } catch (IOException ex) {
                    System.out.println("error");
                    Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        for (int i = 0; i < 1; i++) {
            Thread consumer = new Thread(runnable);
            consumer.start();
        }
    }
}
