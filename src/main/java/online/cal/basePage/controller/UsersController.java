package online.cal.basePage.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;
import online.cal.basePage.model.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class UsersController
{

	@Autowired
	BasePageUserService userService_;
	
	public final String USERS = "users/";
	
	@RequestMapping(value=USERS, method =RequestMethod.GET)
	public List<BasePageUser> getUsers()
	{
		return userService_.getUsers();
	}
}
