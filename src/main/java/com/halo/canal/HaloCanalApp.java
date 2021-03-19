package com.halo.canal;

import com.halo.canal.listener.MessageReceiveListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author shoufeng
 */

@SpringBootApplication
public class HaloCanalApp {

	public static void main(String[] args) {
		SpringApplication.run(HaloCanalApp.class, args);
	}

	@Bean
	public MessageReceiveListener messageReceiveListener() {

		return new MessageReceiveListener();
	}

}
