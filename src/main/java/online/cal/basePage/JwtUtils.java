package online.cal.basePage;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.jetty.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.*;
import org.springframework.security.config.core.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.util.matcher.*;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.*;

import online.cal.basePage.model.*;

public class JwtUtils
{
	private static Algorithm ALGO;

	private static final long TOKEN_LENGTH = 60 * 60;

	private static Algorithm getAlgo()
	{
		if (ALGO == null)
		{
			try
			{
				ALGO = Algorithm.HMAC256("beyondSecret");
			} catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ALGO;
	}

	public static class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter
	{
		public JwtAuthenticationFilter()
		{
			super(new NegatedRequestMatcher(
					new OrRequestMatcher(
					  new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.asString()), // Ignore authentication for CORS requests
					  new AntPathRequestMatcher("/api/v1/sessions/login"),
					  new AntPathRequestMatcher("/api/v1/sessions/loginGuest"),
					  new AntPathRequestMatcher("/error"),
					  new AntPathRequestMatcher("/socket/**"),
					  new AntPathRequestMatcher("/lyrics"))));
			setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler());
			setAuthenticationManager(new AuthProvider());

		}
		
		
		protected boolean requiresAuthentication(HttpServletRequest request,
				HttpServletResponse response) {
			return super.requiresAuthentication(request, response);
		}
		
		

		@Override
		public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
				throws AuthenticationException
		{

			String header = request.getHeader("Authorization");

			if (header == null || !header.startsWith("Bearer "))
			{
				throw new JwtTokenMissingException("No JWT token found in request headers");
			}

			String authToken = header.substring(7);

			try
			{
				JwtAuthenticationToken authRequest = new JwtAuthenticationToken(authToken);

				return getAuthenticationManager().authenticate(authRequest);
			} catch (TokenExpiredException tee)
			{
				throw new JwtTokenExpiredException("Your session has expired. Please log in again");
			}
		}

		@Override
		protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
				FilterChain chain, Authentication authResult) throws IOException, ServletException
		{
			super.successfulAuthentication(request, response, chain, authResult);

			// As this authentication is in HTTP header, after success we need to continue
			// the request normally
			// and return the response as if the resource was not secured at all
			chain.doFilter(request, response);
		}
	}

	public static class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler
	{

		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication)
		{
			// We do not need to do anything extra on REST authentication success, because
			// there is no page to redirect to
		}

	}

	public class NecPageEntryPort implements AuthenticationEntryPoint
	{

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException authException) throws IOException
		{
			// This is invoked when user tries to access a secured REST resource without
			// supplying any credentials
			// We should just send a 401 Unauthorized response because there is no 'login
			// page' to redirect to
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
		}
	}

	public static class AuthProvider implements AuthenticationManager
	{

		public boolean supports(Class<?> authentication)
		{
			return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
		}

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException
		{
			if (!supports(authentication.getClass()))
			{
				throw new JwtTokenMissingException("Unknown Authentication");
			}
			JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
			token.validate();
			return token;
		}
	}

	public static String generateToken(String userName)
	{
		return JWT.create().withIssuer("BasePagePlus").withSubject(userName)
				.withExpiresAt(Date.from(new Date().toInstant().plusSeconds(TOKEN_LENGTH))).sign(getAlgo()); // Expires
																												// in 1
																												// hour
	}

	public static class JwtTokenMissingException extends AuthenticationException
	{
		private static final long serialVersionUID = 1L;

		public JwtTokenMissingException(String msg)
		{
			super(msg);
		}
	}

	public static class JwtTokenExpiredException extends AuthenticationException
	{
		private static final long serialVersionUID = 1L;

		public JwtTokenExpiredException(String msg)
		{
			super(msg);
		}
	}

	public static String getUserFromToken(Object tok)
	{
		if (!(tok instanceof JwtAuthenticationToken))
		{
			throw new JwtTokenMissingException("invalid token");
		}
		JwtAuthenticationToken jtok = (JwtAuthenticationToken) tok;
		jtok.validate();
		return jtok.getName();
	}

	public static class JwtAuthenticationToken implements Authentication
	{
		private static final long serialVersionUID = 1L;
		DecodedJWT decodedToken_;
		String token_;

		public JwtAuthenticationToken(String token)
		{
			token_ = token;
			JWTVerifier verifier = JWT.require(getAlgo()).build();
			decodedToken_ = verifier.verify(token);
		}

		@Override
		public String getName()
		{
			return decodedToken_.getSubject();
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities()
		{
			return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
		}

		@Override
		public Object getCredentials()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getDetails()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getPrincipal()
		{
			return getName();
		}

		@Override
		public boolean isAuthenticated()
		{
			return true;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException
		{
			throw new IllegalArgumentException("Not Supported");
		}

		public void validate()
		{
			if (new Date().after(decodedToken_.getExpiresAt()))
			{
				throw new JwtTokenExpiredException("Your session has expired. Please log in again");
			}
		}
	}
}
