package com.tinmegali.oauth2;

import com.tinmegali.oauth2.models.Account;
import com.tinmegali.oauth2.models.Role;
import com.tinmegali.oauth2.services.AccountService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.util.Arrays;

@SpringBootApplication
@EnableAsync
public class Oauth2Application {

	@Bean
	public BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	public static void main(String[] args) {
		SpringApplication.run(Oauth2Application.class, args);
	}

	@Bean @Qualifier("mainDataSource")
	public DataSource dataSource(){
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder
				.setType(EmbeddedDatabaseType.H2)
				.build();
		return db;
	}

	@Bean
	CommandLineRunner init(
			AccountService accountService
	) {
		return (evt) -> Arrays.asList(
				"user,admin,john,robert,ana".split(",")).forEach(
				username -> {
					Account acct = new Account();
					acct.setUsername(username);
					acct.setPassword("password");
					acct.setFirstName(username);
					acct.setLastName("LastName");
					acct.grantAuthority(Role.ROLE_USER);
					if (username.equals("admin"))
						acct.grantAuthority(Role.ROLE_ADMIN);
					accountService.registerUser(acct);
				}
		);
	}
}
