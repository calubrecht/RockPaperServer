package online.cal.basePage.controller;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;
import online.cal.basePage.model.*;
import online.cal.basePage.model.BasePageUserService.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class SessionController
{
	@Value ( "${app.version}")
	private String appVersion;
	
	public static final String SESSION = "sessions/";
    @Autowired
    AuthenticationManager authenticationManager;
	
	@RequestMapping(SESSION + "userName")
	public String userName()
	{
	  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	  
	  return auth.getName(); 
	}
	
	@RequestMapping(SESSION + "init")
	public String initSession()
	{
	  return "Welcome";
	}
	
	public static class LoginUser
	{
		public String userName;
		public String password;
		public String color;
	}
	

	@RequestMapping(value=SESSION + "logout", method=RequestMethod.POST)
	public ResponseEntity<UserMessage> logout(HttpSession session)
	{
      try
      {
    	 session.invalidate();
		 SecurityContextHolder.getContext().setAuthentication(null);
		 SecurityContextHolder.clearContext();
		 
		 HttpHeaders headers = new HttpHeaders();
		 headers.add("Set-Cookie","platform=mobile; Max-Age=604800; Path=/; Secure; HttpOnly");
		 return ResponseEntity.status(HttpStatus.OK).headers(headers).body(new UserMessage("", ""));
      }
      catch (AuthenticationException ae)
      {
    	  return new ResponseEntity<UserMessage>(new UserMessage(ae.getMessage(), ""), HttpStatus.UNAUTHORIZED);
      }
	}
		
	@RequestMapping(value=SESSION + "register", method=RequestMethod.POST)
	public ResponseEntity<UserMessage> register(HttpSession session, @RequestBody LoginUser user)
	{
      try
      {
    	BasePageUser bpu = new BasePageUser(user.userName, user.password);
    	bpu.setColor(user.color);
        return new ResponseEntity<UserMessage>(
    			  BasePageUserService.getService().register(bpu),
    			  HttpStatus.OK);  
      }
      catch (AuthenticationException ae)
      {
    	  return new ResponseEntity<UserMessage>(new UserMessage(ae.getMessage(), ""), HttpStatus.UNAUTHORIZED);
      }
	}
	
	@RequestMapping(SESSION + "isLoggedIn")
	public boolean isLoggedIn()
	{
		return true;
	}
	
}
