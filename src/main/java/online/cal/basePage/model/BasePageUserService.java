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

import com.fasterxml.jackson.annotation.*;

import online.cal.basePage.*;

public class BasePageUserService
{
	static BasePageUserService INSTANCE;
	WebSecurityConfig securityService_;
	Map<String, BasePageUser> users_ = new HashMap<String, BasePageUser>();
	
	int guestCount = 0;
	
	UserDetailsService service_ = new InMemoryUserDetailsManager(
			
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

		users_.put("luser", new BasePageUser("user", "password"));
		users_.put("bozo", new BasePageUser("user", "password"));
		users_.put("spongey", new BasePageUser("user", "password"));
	}

	public UserDetailsService getUserDetailsService()
	{
		return service_;
	}

	public UserMessage login(String user, String pass)
	{
	   BasePageUser bpu = getUser(user);
	   if (bpu != null && !pass.equals("") && pass.equals(bpu.getPassword()))
	   {
		   String tok = JwtUtils.generateToken(user);
		   return new UserMessage(user, tok);
	   }
	   throw new BPUAuthenticationException("Bad user or password");
	}
	
	public UserMessage createGuest()
	{
	  String name = "Guest-" + ++guestCount;
	  BasePageUser bpu = new BasePageUser(name, "");
	  users_.put(name, bpu);
	  return new UserMessage(name, JwtUtils.generateToken(name));
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
	
	
	public static class UserMessage
	{
		@JsonProperty("userName") public String userName_;
		@JsonProperty("token") public String token_;
		
		public UserMessage(String userName, String token)
		{
			userName_ = userName;
			token_ = token;
		}
	}
}
