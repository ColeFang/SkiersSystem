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

    public RabbitMQChannelPool(RabbitMQChannelPoolFactory factory, GenericObjectPoolConfig poolConfig) {
        this.pool = new GenericObjectPool<Channel>(factory, poolConfig);
    }

    public Channel getChannel() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void returnChannel(Channel channel) {
        if (channel != null) {
            pool.returnObject(channel);
        }
    }
}