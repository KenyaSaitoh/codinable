package pro.kensait.jakartaee.hello.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class HelloBean {
    private String message = "Hello, Alice!";

    public String getMessage() {
        return message;
    }
}
