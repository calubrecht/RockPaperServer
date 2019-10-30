package online.cal.basePage.controller;
import javax.servlet.http.*;

import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;
import online.cal.basePage.AppConstants;
import online.cal.basePage.model.*;

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
	public ResponseEntity<String> login(HttpSession session, @RequestBody BasePageUser user)
	{
      try
      {
    	  return new ResponseEntity<String>(
    			  BasePageUserService.getService().login(user.getUserName(), user.getPassword()),
    			  HttpStatus.OK);
      }
      catch (AuthenticationException ae)
      {
    	  return new ResponseEntity<String>("whoare are you?", HttpStatus.UNAUTHORIZED);
      }
	}
	
	@RequestMapping(SESSION + "isLoggedIn")
	public boolean isLoggedIn()
	{
		return true;
	}
	
}
