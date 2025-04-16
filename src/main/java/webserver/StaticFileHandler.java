package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticFileHandler {
    private static final Logger log = LoggerFactory.getLogger(StaticFileHandler.class);

    public void serve(File file, DataOutputStream dos) throws IOException {
        byte[] body = Files.readAllBytes(file.toPath());

        String contentType = guessContentType(file.getName());
        response200Header(dos, body.length, contentType);
        responseBody(dos, body);
    }

    private String guessContentType(String filename) {
        String contentType = "text/html;charset=utf-8"; // 기본값

        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return contentType;
        }

        String ext = filename.substring(dotIndex + 1);
        switch (ext) {
            case "css":
                contentType = "text/css";
                break;
            case "js":
                contentType = "application/javascript";
                break;
        }
        return contentType;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }
}
