/**
 * @program: server
 * @description:
 * @author: Mr.Fang
 * @create: 2022-03-09 13:40
 **/
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RabbitMQChannelPool {
    private GenericObjectPool<Channel> pool;

    /**
    * @Description: 
    * @Param: [factory, poolConfig]
    * @return: 
    */
    public RabbitMQChannelPool(RabbitMQChannelPoolFactory factory, GenericObjectPoolConfig poolConfig) {
        this.pool = new GenericObjectPool<Channel>(factory, poolConfig);
    }

    /**
    * @Description: 
    * @Param: []
    * @return: com.rabbitmq.client.Channel
    */
    public Channel getChannel() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
    * @Description: 
    * @Param: [channel]
    * @return: void
    */
    public void returnChannel(Channel channel) {
        if (channel != null) {
            pool.returnObject(channel);
        }
    }
}