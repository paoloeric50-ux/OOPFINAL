package com.motorph.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * MotorPhApplication.java - Entry point for the Spring Boot application
 *
 * This is the main class that starts everything. When you run the JAR file,
 * the JVM calls main(), which tells Spring Boot to start up.
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *   @Configuration        - this class can define beans
 *   @EnableAutoConfiguration - Spring auto-configures itself based on dependencies
 *   @ComponentScan        - Spring scans this package for @Component, @Service, etc.
 *
 * Spring Boot then:
 *   1. Starts an embedded Tomcat server on port 5000
 *   2. Connects to the H2 database and creates tables
 *   3. Sets up the security filter chain
 *   4. Registers all controllers, services, and repositories
 *
 * Author: Group 5 - MO-IT101
 */
@SpringBootApplication
public class MotorPhApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotorPhApplication.class, args);
    }
}
