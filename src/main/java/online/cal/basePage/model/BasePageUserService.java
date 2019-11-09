package online.cal.basePage.model;


import java.security.*;
import java.security.spec.*;
import java.util.*;

import javax.annotation.*;

import org.bson.Document;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.*;
import org.springframework.security.web.context.*;
import org.springframework.stereotype.*;

import com.fasterxml.jackson.annotation.*;
import com.mongodb.client.*;

import online.cal.basePage.*;

@Component
public class BasePageUserService
{
	static BasePageUserService INSTANCE;
	WebSecurityConfig securityService_;
	Map<String, BasePageUser> users_ = new HashMap<String, BasePageUser>();
	
	@Autowired
	DBStore dbStore_;
	
	int guestCount = 0;
	
	
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

	}
	
	@PostConstruct
	public void init()
	{
	   byte[] salt = BasePageUser.generateSalt();
	   try
	{
		String hash = BasePageUser.hashPassword("password", salt);
		String saltString = Base64.getEncoder().encodeToString(salt);
		BasePageUser b=null;
	} catch (InvalidKeySpecException | NoSuchAlgorithmException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   MongoIterable<Document> itr = dbStore_.getCollection("users").find();
	   for (Document d : itr)
	   {
		   String name = d.getString("userName");
		   String passwordHash = d.getString("passwordHash");
		   String passwordSalt = d.getString("passwordSalt");
		   users_.put(name, new BasePageUser(name, passwordHash, passwordSalt));
	   }
	}

	public UserMessage login(String user, String pass)
	{
	   BasePageUser bpu = getUser(user);
	   if (bpu != null && !pass.equals("") && bpu.validatePassword(pass))
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
