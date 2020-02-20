package online.cal.basePage;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.*;
import org.springframework.security.web.csrf.*;

import online.cal.basePage.model.*;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	BasePageUserService service_ = new BasePageUserService(this);

	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    	http.addFilterBefore(new JwtUtils.JwtAuthenticationFilter(), BasicAuthenticationFilter.class);    	
    }
    

}