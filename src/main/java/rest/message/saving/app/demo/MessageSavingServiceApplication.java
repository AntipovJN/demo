package rest.message.saving.app.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MessageSavingServiceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MessageSavingServiceApplication.class, args);
	}

}
