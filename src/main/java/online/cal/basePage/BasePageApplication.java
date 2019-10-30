package online.cal.basePage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.*;
import org.springframework.boot.web.servlet.support.*;

@SpringBootApplication
public class BasePageApplication extends SpringBootServletInitializer 
{
	public static void main(String[] args)
	{
		SpringApplication.run(BasePageApplication.class, args);
	}
	
	
	   @Override
	    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	        return application.sources(BasePageApplication.class);
	    }
}
