package io.testrail.company.webapp.webappAtCoverage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WebappAtCoverageApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebappAtCoverageApplication.class, args);
    }
}
