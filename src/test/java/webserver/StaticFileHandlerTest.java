package webserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.junit.Test;

public class StaticFileHandlerTest {

    @Test
    public void HTML_파일을_응답으로_보내야_함() throws IOException {
        // given: 임시 파일 생성
        File tempHtml = File.createTempFile("test", ".html");
        tempHtml.deleteOnExit();

        try (Writer writer = new FileWriter(tempHtml)) {
            writer.write("<html><body>Hello</body></html>");
        }

        // when
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        new StaticFileHandler().serve(tempHtml, dos);

        String result = out.toString("UTF-8");

        // then
        assertThat(result).contains("HTTP/1.1 200 OK");
        assertThat(result).contains("Content-Type: text/html");
        assertThat(result).contains("Content-Length:"); // 길이는 바뀔 수 있으니 헤더만 체크
        assertThat(result).contains("<html><body>Hello</body></html>");
    }

}
