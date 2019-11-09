package online.cal.basePage.controller;
import javax.servlet.http.*;

import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.*;

import online.cal.basePage.AppConstants;
import online.cal.basePage.model.*;
import online.cal.basePage.model.BasePageUserService.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class SessionController
{
	public static final String SESSION = "sessions/";
	
	@RequestMapping(SESSION + "userName")
	public String userName()
	{
	  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	  
	  return auth.getName(); 
	}
	
	@RequestMapping(value=SESSION + "login", method=RequestMethod.POST)
	public ResponseEntity<UserMessage> login(HttpSession session, @RequestBody BasePageUser user)
	{
      try
      {
    	  return new ResponseEntity<UserMessage>(
    			  BasePageUserService.getService().login(user.getUserName(), user.getPassword()),
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
   	  return new ResponseEntity<UserMessage>(
   			  BasePageUserService.getService().createGuest(),
   			  HttpStatus.OK);
    }
	
	@RequestMapping(SESSION + "isLoggedIn")
	public boolean isLoggedIn()
	{
		return true;
	}
	
}
