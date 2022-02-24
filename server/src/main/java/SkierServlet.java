import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "Servlet", value = "/Servlet")
public class SkierServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check URL
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            res.getWriter().write(urlPath);
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("invalid url");
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("url accepted");
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        BufferedReader post = req.getReader();
        String urlPath = req.getPathInfo();

        // check URL
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            res.getWriter().write(urlPath);
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (checkJson(post) && isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("data accepted");
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("invalid data");
        }
    }
    private boolean checkJson(BufferedReader post) throws IOException {
        String[] keys={"time","liftID","waitTime"};
        List<String> tempList = Arrays.asList(keys);
        String str=null;
        String key;
        String value;
        while (str==null){
            str=post.readLine();
        }
        String[] temp = str.split(",");
        if (temp.length != 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            key = temp[i].split("\"")[1];
            value = temp[i].split(":")[1];
            value = value.replace("}","");
            if (!tempList.contains(key)) {
                return false;
            } else if (key.equals("time") && !value.matches("\\d+")) {
                return false;
            } else if (key.equals("liftID") && !value.matches("\\d+")) {
                return false;
            } else if (key.equals("waitTime") && !value.matches("\\d+")) {
                return false;
            }
        }
        return true;
    }

    private boolean isUrlValid(String[] urlPath) {
        if (urlPath.length!=8){
            return false;
        }
        boolean valid = true;
        valid = valid && (urlPath[2].equals("seasons")) && (urlPath[4].equals("days")) && (urlPath[6].equals("skiers"));
        valid = valid &&  urlPath[1].matches("\\d+") &&  urlPath[3].matches("\\d+") &&  urlPath[5].matches("\\d+") &&  urlPath[7].matches("\\d+");
        valid = valid && (Integer.parseInt(urlPath[5])>=1) && (Integer.parseInt(urlPath[5])<=366);
        return valid;
    }
}
