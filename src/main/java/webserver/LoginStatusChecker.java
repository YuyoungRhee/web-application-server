package webserver;

import java.util.List;
import java.util.Map;
import util.HttpRequestUtils;

public class LoginStatusChecker {

    public boolean isLogined(List<String> requestHeaders) {
        for (String header : requestHeaders) {
            if (header.startsWith("Cookie:")) {
                Map<String, String> cookies = HttpRequestUtils.parseCookies(header);
                String logined = cookies.get("logined");
                return Boolean.parseBoolean(logined);
            }
        }
        return false;

    }
}
