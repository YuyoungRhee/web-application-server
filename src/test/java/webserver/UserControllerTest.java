package webserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import org.junit.Test;

public class UserControllerTest {

    @Test
    public void 유저_생성시_201응답_보내야함() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        String body = "userId=test&password=1234&name=홍길동&email=hong@test.com";

        new UserController().createUser(dos, body);

        String result = out.toString("UTF-8");
        assertThat(result).contains("201 Created");
    }

}
