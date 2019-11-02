package online.cal.basePage.model;


import java.util.*;
import java.util.stream.*;

import javax.servlet.http.*;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.*;
import org.springframework.security.web.context.*;

import online.cal.basePage.*;

public class BasePageUserService
{
	static BasePageUserService INSTANCE;
	WebSecurityConfig securityService_;
	Map<String, BasePageUser> users_ = new HashMap<String, BasePageUser>();
	
	UserDetailsService service_ = new InMemoryUserDetailsManager(
			
			User.withUsername("user").password("{noop}password").roles("USER").build(),
			User.withUsername("luser").password("{noop}password").roles("USER").build(),
			User.withUsername("bozo").password("{noop}password").roles("USER").build(),
			User.withUsername("spongey").password("{noop}password").roles("USER").build()
			);
	
	public static BasePageUserService getService()
	{
		assert INSTANCE != null;
		return INSTANCE;
	}


	public BasePageUserService(WebSecurityConfig securityService)
	{
		securityService_ = securityService;
		assert INSTANCE == null;
		INSTANCE = this;

		users_.put("user", new BasePageUser("user", "password"));
		users_.put("luser", new BasePageUser("user", "password"));
		users_.put("bozo", new BasePageUser("user", "password"));
		users_.put("spongey", new BasePageUser("user", "password"));
	}

	public UserDetailsService getUserDetailsService()
	{
		return service_;
	}

	public String login(String user, String pass)
	{
	   BasePageUser bpu = getUser(user);
	   if (bpu != null && pass.equals(bpu.getPassword()))
	   {
		   return JwtUtils.generateToken(user);
	   }
	   throw new BPUAuthenticationException("Bad user or password");
	}
	
	BasePageUser getUser(String name)
	{
		return users_.get(name);
	}
	
	public static class BPUAuthenticationException extends AuthenticationException
	{
		public BPUAuthenticationException(String err)
		{
			super(err);
		}
	}
}
