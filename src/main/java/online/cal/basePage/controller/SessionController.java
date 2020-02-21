package online.cal.basePage.controller;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.AppConstants;
import online.cal.basePage.model.*;
import online.cal.basePage.model.BasePageUserService.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class SessionController
{
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
	
	@RequestMapping(value=SESSION + "login", method=RequestMethod.POST)
	public ResponseEntity<UserMessage> login(HttpSession session, @RequestBody LoginUser user)
	{
      try
      {
 		 UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.userName, user.password);
		 token.setDetails(BasePageUserService.getService().getUser(user.userName));
		 SecurityContextHolder.getContext().setAuthentication(token);
    	 return new ResponseEntity<UserMessage>(
    			  BasePageUserService.getService().login(user.userName, user.password),
    			  HttpStatus.OK);
      }
      catch (AuthenticationException ae)
      {
    	  return new ResponseEntity<UserMessage>(new UserMessage(ae.getMessage(), ""), HttpStatus.UNAUTHORIZED);
      }
	}
	
	@RequestMapping(value=SESSION + "loginGuest", method=RequestMethod.POST)
	public ResponseEntity<UserMessage> loginGuest(HttpSession session)
	{
	  UserMessage um =  BasePageUserService.getService().createGuest();
	  UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(um.userName_,"");
	  token.setDetails(BasePageUserService.getService().getUser(um.userName_));
	  SecurityContextHolder.getContext().setAuthentication(token);
   	  return new ResponseEntity<UserMessage>(um, HttpStatus.OK);
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
