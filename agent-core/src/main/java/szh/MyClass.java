package szh;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyClass {
    HttpServletRequest request;
    HttpServletResponse response;
    public MyClass(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
    public void act() {
        System.out.println(response.getStatus());
        System.out.println(request.getRequestURL());
    }
}
