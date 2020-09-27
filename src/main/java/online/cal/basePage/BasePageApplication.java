package online.cal.basePage;


import javax.servlet.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.*;
import org.springframework.boot.builder.*;
import org.springframework.boot.context.event.*;
import org.springframework.boot.web.servlet.support.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.security.web.authentication.session.*;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class BasePageApplication extends SpringBootServletInitializer
{
    Logger logger = LoggerFactory.getLogger(getClass());
	@Value ( "${app.version}")
	private String appVersion;
	
	static BasePageApplication instance;
	
	public BasePageApplication()
	{
		instance = this;
	}
	
	public static void main(String[] args)
	{
		SpringApplication app = new SpringApplication(new Class[] {BasePageApplication.class});
		app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
			logVersion(); });
		app.run(args);
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
	
	public static void logVersion()
	{
		if (instance != null)
		{
			instance.logger.info("Started BasePageApplication v:{}", instance.appVersion);
		}
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException 
	{
		super.onStartup(servletContext);
        logger.info("Started BasePageApplcation v:{}", appVersion);
	}
}
