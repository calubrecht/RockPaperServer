package online.cal.basePage;

import org.eclipse.jetty.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.util.matcher.*;

import online.cal.basePage.controller.*;
import online.cal.basePage.model.*;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    BasePageUserService userService_;
    
    @Autowired
	JwtUtils jwtUtils;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
		http.authorizeRequests()
				.requestMatchers(
					new OrRequestMatcher(
						new AntPathRequestMatcher("/api/v1/sessions/login")),
						new AntPathRequestMatcher("/api/v1/sessions/loginGuest"),
						new AntPathRequestMatcher("/api/v1/sessions/register"),
						new AntPathRequestMatcher("/api/v1/sessions/init"),
//						new AntPathRequestMatcher("/socket/**"),
						new AntPathRequestMatcher("/error"),
						new AntPathRequestMatcher("/lyrics"),
						// Ignore for CORS requests
						new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.asString())).permitAll()
				.antMatchers("/api/v1/**").hasRole("USER");
		JsonAuthenticationFilter filter = new JsonAuthenticationFilter(AppConstants.API_PATH + SessionController.SESSION + "login", authenticationManager, jwtUtils);
		http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
		JsonAuthenticationFilter.GuestAuthFilter guestFilter = new JsonAuthenticationFilter.GuestAuthFilter(AppConstants.API_PATH + SessionController.SESSION + "loginGuest", authenticationManager,  userService_, jwtUtils);
		http.addFilterBefore(guestFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	

}