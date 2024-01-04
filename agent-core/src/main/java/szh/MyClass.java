package szh;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyClass {
    private static ILog LOGGER = LogManager.getLogger(MyClass.class);
    HttpServletRequest request;
    HttpServletResponse response;
    public MyClass(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
    public void echo() {
        //LOGGER.info(MyClass.class.getClassLoader().toString());

        LOGGER.info("request: " + request + ", response: " + response);
    }
}
