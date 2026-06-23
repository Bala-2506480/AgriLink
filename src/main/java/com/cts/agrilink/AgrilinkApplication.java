package com.cts.agrilink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.cts.agrilink", "com.agrilink.produce_module"})
@EnableJpaRepositories(basePackages = {"com.cts.agrilink", "com.agrilink.produce_module"})
public class AgrilinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgrilinkApplication.class, args);
	}

}
