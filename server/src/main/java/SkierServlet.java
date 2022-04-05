//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@WebServlet(
        name = "Servlet",
        value = {"/Servlet"}
)
public class SkierServlet extends HttpServlet {
    private static final String EXCHANGE_NAME = "exchange";
    private static final String ROUTING_KEY = "routing_key";
    private static final String IP_ADDRESS = "172.31.31.193";
    private static final int MAX_CHANNEL = 8;
    private static final int MIN_CHANNEL = 0;
    private static final int PORT = 5672;
    private static ConnectionFactory factory;
    private static RabbitMQChannelPool rabbitMQChannelPool;
    private String jsonMessage;

    /**
    * @Description: 
    * @Param: []
    * @return: 
    */
    public SkierServlet() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("root");
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(8);
        config.setMaxIdle(8);
        config.setMinIdle(0);
        rabbitMQChannelPool = new RabbitMQChannelPool(new RabbitMQChannelPoolFactory(factory), config);
    }

    /**
    * @Description: 
    * @Param: [req, res]
    * @return: void
    */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Channel channel = rabbitMQChannelPool.getChannel();
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        if (urlPath != null && !urlPath.isEmpty()) {
            this.jsonMessage = "";
            String[] urlParts = urlPath.split("/");
            if (!this.isUrlValid(urlParts)) {
                res.setStatus(404);
                res.getWriter().write("invalid url");
            } else {
                res.setStatus(200);
                res.getWriter().write("url accepted");
                channel.basicPublish("exchange", "routing_key", MessageProperties.PERSISTENT_TEXT_PLAIN, this.getMessage(urlPath));
            }
        } else {
            res.setStatus(404);
            res.getWriter().write("missing paramterers");
            res.getWriter().write(urlPath);
        }
        rabbitMQChannelPool.returnChannel(channel);
    }

    /**
    * @Description: 
    * @Param: [req, res]
    * @return: void
    */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Channel channel = rabbitMQChannelPool.getChannel();
        res.setContentType("text/plain");
        BufferedReader post = req.getReader();
        String urlPath = req.getPathInfo();
        if (urlPath != null && !urlPath.isEmpty()) {
            this.jsonMessage = "";
            String[] urlParts = urlPath.split("/");
            if (this.checkJson(post) && this.isUrlValid(urlParts)) {
                res.setStatus(200);
                res.getWriter().write("data accepted");
                channel.basicPublish("exchange", "routing_key", MessageProperties.PERSISTENT_TEXT_PLAIN, this.getMessage(urlPath));
            } else {
                res.setStatus(404);
                res.getWriter().write("invalid data");
            }

        } else {
            res.setStatus(404);
            res.getWriter().write("missing paramterers");
            res.getWriter().write(urlPath);
        }
        rabbitMQChannelPool.returnChannel(channel);
    }

    /**
    * @Description: 
    * @Param: [post]
    * @return: boolean
    */
    private boolean checkJson(BufferedReader post) throws IOException {
        String[] keys = new String[]{"time", "liftID", "waitTime"};
        List<String> tempList = Arrays.asList(keys);

        String str;
        for(str = null; str == null; str = post.readLine()) {
        }

        String[] temp = str.split(",");
        if (temp.length != 3) {
            return false;
        } else {
            String[] values = new String[3];

            for(int i = 0; i < 3; ++i) {
                String key = temp[i].split("\"")[1];
                String value = temp[i].split(":")[1];
                value = value.replace("}", "");
                values[i] = value;
                if (!tempList.contains(key)) {
                    return false;
                }

                if (key.equals("time") && !value.matches("\\d+")) {
                    return false;
                }

                if (key.equals("liftID") && !value.matches("\\d+")) {
                    return false;
                }

                if (key.equals("waitTime") && !value.matches("\\d+")) {
                    return false;
                }
            }

            this.jsonMessage = "/time/" + values[0] + "/liftID/" + values[1] + "/waitTime/" + values[2];
            return true;
        }
    }

    /**
    * @Description: 
    * @Param: [url]
    * @return: byte[]
    */
    private byte[] getMessage(String url) throws IOException {
        String message = "resorts" + url + this.jsonMessage;
        return message.getBytes(StandardCharsets.UTF_8);
    }

    /**
    * @Description: 
    * @Param: [urlPath]
    * @return: boolean
    */
    private boolean isUrlValid(String[] urlPath) {
        if (urlPath.length != 8) {
            return false;
        } else {
            boolean valid = true;
            valid = valid && urlPath[2].equals("seasons") && urlPath[4].equals("days") && urlPath[6].equals("skiers");
            valid = valid && urlPath[1].matches("\\d+") && urlPath[3].matches("\\d+") && urlPath[5].matches("\\d+") && urlPath[7].matches("\\d+");
            valid = valid && Integer.parseInt(urlPath[5]) >= 1 && Integer.parseInt(urlPath[5]) <= 366;
            return valid;
        }
    }
}
