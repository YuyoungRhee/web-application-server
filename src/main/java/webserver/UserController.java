package webserver;

import db.DataBase;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public void handle(String url, DataOutputStream dos, String body) {
        int firstSlash = url.indexOf("/");           // 0
        int secondSlash = url.indexOf("/", firstSlash + 1);
        String root = url.substring(0, secondSlash);
        if(!root.equals("/user")) {
            throw new IllegalArgumentException("user 경로가 아닌 요청입니다.");
        }

        String path = url.substring(secondSlash);
        if (path.equals("/create")) {
            createUser(dos, body);
        }
        if(path.equals("/login")) {
            loginUser(dos, body);
        }
    }

    private void createUser(DataOutputStream dos, String body) {
        String decoded = decode(body);

        Map<String, String> paramDatas = HttpRequestUtils.parseQueryString(decoded);

        String userId = paramDatas.get("userId");
        String password = paramDatas.get("password");
        String name = paramDatas.get("name");
        String email = paramDatas.get("email");

        User user = new User(userId, password, name, email);
        DataBase.addUser(user);

        redirectTo(dos, "/index.html");
    }

    private void loginUser(DataOutputStream dos, String body) {
        String decoded = decode(body);

        Map<String, String> paramDatas = HttpRequestUtils.parseQueryString(decoded);

        String userId = paramDatas.get("userId");
        String password = paramDatas.get("password");

        Optional<User> result = DataBase.findUserById(userId);
        if(!result.isPresent()) {
            log.error("해당 id의 user가 존재하지 않습니다.");
            responseLoginFailed(dos);
            return;
        }

        User user = result.get();
        if(password.equals(user.getPassword())) {
            redirectWithCookie(dos, "/index.html");
        }
    }

    private void responseLoginFailed(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
            dos.writeBytes("Content-Type: text/html\r\n");
            dos.writeBytes("\r\n");
            File file = new File("./webapp/user/login_failed.html");
            byte[] body = Files.readAllBytes(file.toPath());

            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void redirectTo(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void redirectWithCookie(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 303 See Other\r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true; Path=/; SameSite=Lax;\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
//    private void response201Header(DataOutputStream dos) {
//        int lengthOfBodyContent = 0;
//
//        try {
//            dos.writeBytes("HTTP/1.1 201 Created \r\n");
//            dos.writeBytes("\r\n");
//            dos.flush();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            throw new IllegalStateException(e);
//        }

//    }
}
