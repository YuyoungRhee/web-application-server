package webserver;

import static org.assertj.core.api.Assertions.assertThat;

import db.DataBase;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import model.User;
import org.junit.Test;

public class UserControllerTest {

    @Test
    public void 유저_생성시_302응답_리다이렉트() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        String url = "/user/create";
        String body = "userId=test&password=1234&name=홍길동&email=hong@test.com";

        new UserController().handle(url, dos, body);

        String result = out.toString("UTF-8");
        assertThat(result).contains("302 Found");
        assertThat(result).contains("Location: /index.html");
    }

    @Test
    public void 유저_로그인_성공시_쿠키와함께_리다이렉트() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        DataBase.addUser(new User("testId", "1234", "testUser", "test@email.com"));
        String url = "/user/login";
        String body = "userId=testId&password=1234";

        new UserController().handle(url, dos, body);

        String result = out.toString("UTF-8");
        assertThat(result).contains("303 See Other");
        assertThat(result).contains("Location: /index.html");
        assertThat(result).contains("logined=true"); //Set-Cookie
    }

    @Test
    public void 유저_로그인_실패시_404() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        String url = "/user/login";
        String body = "userId=noId&password=1234";

        new UserController().handle(url, dos, body);

        String result = out.toString("UTF-8");
        assertThat(result).contains("404 Not Found");
    }

}
