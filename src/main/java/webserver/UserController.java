package webserver;

import db.DataBase;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public void handle(String url, DataOutputStream dos, String body) {
        int firstSlash = url.indexOf("/");           // 0
        int secondSlash = url.indexOf("/", firstSlash + 1);

        String path = url.substring(secondSlash);
        if (path.equals("/create")) {
            createUser(dos, body);
        }
    }

    public void createUser(DataOutputStream dos, String body) {

        String decoded = decode(body);

        Map<String, String> paramDatas = HttpRequestUtils.parseQueryString(decoded);

        String userId = paramDatas.get("userId");
        String password = paramDatas.get("password");
        String name = paramDatas.get("name");
        String email = paramDatas.get("email");

        User user = new User(userId, password, name, email);
        DataBase.addUser(user);

        response201Header(dos);
//        responseBody(dos, body);
    }

    private String decode(String body) {
        String decoded = "";
        try {
            decoded = URLDecoder.decode(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("디코딩 실패", e.getMessage());
            throw new IllegalArgumentException("디코딩 실패", e);
        }
        return decoded;
    }

    private void response201Header(DataOutputStream dos) {
        int lengthOfBodyContent = 0;

        try {
            dos.writeBytes("HTTP/1.1 201 Created \r\n");
//            dos.writeBytes("Location: ");
//            dos.writeBytes("Content-Type: application/json \r\n");
//            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }
}
