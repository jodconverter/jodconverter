package org.artofsolving.jodconverter.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot main application class. Serves as both the runtime application entry point and the central Java
 * configuration class.
 */
@SpringBootApplication
public class Application {

    /**
     * Entry point for the application.
     * 
     * @param args Command line arguments.
     * @throws Exception Thrown when an unexpected Exception is thrown from the application.
     */
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
