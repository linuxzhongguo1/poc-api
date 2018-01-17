package io.mithrilcoin.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@EnableCaching
@ComponentScan("io.mithrilcoin.*")
@MapperScan("io.mithrilcoin.*")
@SpringBootApplication
public class MithrilplayapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MithrilplayapiApplication.class, args);
	}
}
