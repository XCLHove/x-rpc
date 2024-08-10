package com.xclhove.consumer.springboot;

import com.xclhove.rpc.springboot.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xclhove
 */
@SpringBootApplication
@EnableRpc(needServer = false)
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
