package org.example.personalworkworkspace;

import lombok.ToString;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.example")
public class PersonalWorkWorkspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalWorkWorkspaceApplication.class, args);
    }

}
