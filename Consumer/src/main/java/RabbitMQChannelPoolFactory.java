/**
 * @program: server
 * @description:
 * @author: Mr.Fang
 * @create: 2022-03-09 13:32
 **/
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQChannelPoolFactory implements PooledObjectFactory<Channel> {
    private Connection connection;
    private static final String EXCHANGE_NAME = "exchange";
    private static final String ROUTING_KEY = "routing_key";
    private static final String QUEUE_NAME = "lifts_queue";
    public RabbitMQChannelPoolFactory(ConnectionFactory factory) throws IOException, TimeoutException {
        this.connection = factory.newConnection();
    }

    @Override
    public PooledObject<Channel> makeObject() throws Exception {
        DefaultPooledObject<Channel> channel = new DefaultPooledObject<Channel>(connection.createChannel());
        channel.getObject().exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
        channel.getObject().queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.getObject().queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        return channel;
    }

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        if (p != null && p.getObject() != null && p.getObject().isOpen()) {
            p.getObject().close();
        }
    }

    @Override
    public boolean validateObject(PooledObject<Channel> p) {
        return p.getObject() != null && p.getObject().isOpen();
    }

    @Override
    public void activateObject(PooledObject<Channel> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Channel> p) throws Exception {

    }
    public void closeConnection() throws IOException {
        connection.close();
    }
}