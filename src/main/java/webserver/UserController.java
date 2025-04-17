package webserver;

import db.DataBase;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public void handle(String url, DataOutputStream dos, List<String> requestHeaders, String requestBody) {
        int firstSlash = url.indexOf("/");           // 0
        int secondSlash = url.indexOf("/", firstSlash + 1);
        String root = url.substring(0, secondSlash);
        if(!root.equals("/user")) {
            throw new IllegalArgumentException("user 경로가 아닌 요청입니다.");
        }

        String path = url.substring(secondSlash);
        if (path.equals("/create")) {
            createUser(dos, requestBody);
        }
        if(path.equals("/login")) {
            loginUser(dos, requestBody);
        }
        if(path.equals("/list")) {
            showUserList(dos, requestHeaders);
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

    private void showUserList(DataOutputStream dos, List<String> requestHeaders) {
        LoginStatusChecker loginStatusChecker = new LoginStatusChecker();
        boolean logined = loginStatusChecker.isLogined(requestHeaders);
        if(!logined) {
            send401Error(dos);
            return;
        }

        try {
            Collection<User> allUsers = DataBase.findAll();
            String userRowsHtml = renderUserRows(allUsers);

            String html = readHtmlTemplate("./webapp/user/list.html")
                    .replace("{userTableRows}", userRowsHtml);

            byte[] body = html.getBytes(StandardCharsets.UTF_8);

            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: text/html; charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");
            dos.write(body);
            dos.flush();

        } catch (IOException e) {
            log.error("유저 리스트 출력 실패", e);
            throw new IllegalStateException(e);
        }
    }

    private void send401Error(DataOutputStream dos) {
        try {
            String body = "<html><body><h1>401 Unauthorized</h1><p>로그인이 필요합니다.</p></body></html>";
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            dos.writeBytes("HTTP/1.1 401 Unauthorized\r\n");
            dos.writeBytes("Content-Type: text/html; charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + bodyBytes.length + "\r\n");
            dos.writeBytes("\r\n");
            dos.write(bodyBytes);
            dos.flush();
        } catch (IOException e) {
            log.warn("401 Error: {}", e.getMessage());
        }
    }

    private String renderUserRows(Collection<User> users) {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (User user : users) {
            sb.append("<tr>")
                    .append("<th scope=\"row\">").append(index++).append("</th>")
                    .append("<td>").append(user.getUserId()).append("</td>")
                    .append("<td>").append(user.getName()).append("</td>")
                    .append("<td>").append(user.getEmail()).append("</td>")
                    .append("<td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    private String readHtmlTemplate(String path) throws IOException {
        File file = new File(path);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
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
