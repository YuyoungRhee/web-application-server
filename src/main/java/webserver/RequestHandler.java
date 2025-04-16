package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String line = bufferedReader.readLine();

            if(line == null || line.isEmpty()) {
                send400Error(dos);
                return;
            }

            String[] tokens = line.split(" ");
            String url = tokens[1];

            log.info("url: " + url);
            File file = new File("./webapp" + url);
            byte[] body = Files.readAllBytes(file.toPath());

            String contentType = guessContentType(file.getName());
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void send400Error(DataOutputStream dos) throws IOException {
        String body = "400 Bad Request";
        dos.writeBytes("HTTP/1.1 400 Bad Request\r\n");
        dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
        dos.writeBytes("Content-Length: " + body.getBytes().length + "\r\n");
        dos.writeBytes("\r\n");
        dos.writeBytes(body);
        dos.flush();
    }
}
