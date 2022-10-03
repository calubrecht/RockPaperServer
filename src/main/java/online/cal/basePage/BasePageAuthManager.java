package online.cal.basePage;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;

import online.cal.basePage.model.*;

@Configuration
public class BasePageAuthManager implements AuthenticationManager
{
    Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	BasePageUserService userService_;
	
	static final GrantedAuthority USER = new  SimpleGrantedAuthority("ROLE_USER");
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		if (!(authentication instanceof UsernamePasswordAuthenticationToken))
		{
			throw new BadCredentialsException("Invalid authentication");
		}
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;
		BasePageUser user = userService_.getUser(token.getName());
		if (user == null || !user.isGuest() && !user.validatePassword((String) token.getCredentials()))
		{
			logger.error("Bad password for " + user);
			throw new BadCredentialsException("Invalid authentication");
		}
		
		return new UsernamePasswordAuthenticationToken(token.getName(), null, Collections.singletonList(USER));
	}
}
