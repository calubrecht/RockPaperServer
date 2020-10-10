package online.cal.basePage;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.json.*;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.authentication.session.*;

import com.fasterxml.jackson.annotation.*;

import online.cal.basePage.model.*;
import online.cal.basePage.model.BasePageUserService.*;

public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter
{
    
	protected JsonAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager mgr)
	{
		super(defaultFilterProcessesUrl);
		setAuthenticationManager(mgr);
		setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
		setAuthenticationSuccessHandler(new JsonSuccessHandler());
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException
	{
		if (!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException(
					"Authentication method not supported: " + request.getMethod());
		}

		UsernamePasswordAuthenticationToken authRequest = getAuthToken(request);

		authRequest.setDetails(request);

		return this.getAuthenticationManager().authenticate(authRequest);
	}

	protected UsernamePasswordAuthenticationToken getAuthToken(HttpServletRequest request) throws IOException
	{
		String sData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		JsonParser springParser = JsonParserFactory.getJsonParser();
		Map<String, Object> map = springParser.parseMap(sData);
		String username = map.getOrDefault("userName", "").toString();
		String password = map.getOrDefault("password", "").toString();
		// TODO Auto-generated method stub
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				username, password);
		return authRequest;
	}
	
	private static class JsonSuccessHandler implements AuthenticationSuccessHandler
	{

		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication) throws IOException, ServletException
		{
			 String tok = JwtUtils.generateToken(authentication.getName());
			 HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
		        Writer out = responseWrapper.getWriter();
		        out.write("{\"userName\": \"" + authentication.getName() + "\", \"token\":\"" + tok + "\"}");
		        out.close();
			
		}
		
	}
	
	public static class GuestAuthFilter extends JsonAuthenticationFilter
	{
		BasePageUserService userService_;
		GuestAuthFilter(String path, AuthenticationManager mgr, BasePageUserService userService)
		{
			super(path, mgr);
			userService_ = userService;
		}
		
		@Override
		protected UsernamePasswordAuthenticationToken getAuthToken(HttpServletRequest request) throws IOException
		{
			String username =  userService_.createGuest();
			String password = "";
			// TODO Auto-generated method stub
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
					username, password);
			return authRequest;
		}
	}
}
