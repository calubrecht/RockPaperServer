package online.cal.basePage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.*;
import org.springframework.boot.builder.*;
import org.springframework.boot.web.servlet.support.*;
import org.springframework.context.annotation.*;
import org.springframework.security.web.authentication.session.*;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class BasePageApplication extends SpringBootServletInitializer
{
	public static void main(String[] args)
	{
		SpringApplication.run(BasePageApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(BasePageApplication.class);
	}
	
	
	@Bean
	public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new SessionFixationProtectionStrategy();
	}
}
