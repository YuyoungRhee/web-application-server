package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final UserController userController = new UserController();
    private final StaticFileHandler staticFileHandler = new StaticFileHandler();

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

            if(file.exists()) {
                staticFileHandler.serve(file, dos);
                return;
            }
            userController.createUser(url, dos);

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private void send400Error(DataOutputStream dos) throws IOException {
        String body = "400 Bad Request";
        try {
            dos.writeBytes("HTTP/1.1 400 Bad Request\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + body.getBytes().length + "\r\n");
            dos.writeBytes("\r\n");
            dos.writeBytes(body);
            dos.flush();
        } catch (IOException e) {
            log.warn("Broken pipe or other IO error while sending 400: {}", e.getMessage());
        }
    }
}
