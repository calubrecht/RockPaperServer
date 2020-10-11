package online.cal.basePage;

import java.security.*;
import java.time.*;
import java.util.*;

import org.bson.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.stereotype.*;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.*;
import com.mongodb.client.*;

import online.cal.basePage.model.*;

/**
 * JwtUtils, used to make a tokens for Websocket authentication
 */
@Component
public class JwtUtils
{
	@Autowired
	JwtByteSource byteSource;
	
	private Algorithm ALGO;

	private static final long TOKEN_LENGTH = 60 * 60;

	private Algorithm getAlgo()
	{
		if (ALGO == null)
		{
			refreshAlgo();
		}
		return ALGO;
	}

	private synchronized void refreshAlgo()
	{
		ALGO =  Algorithm.HMAC256(byteSource.getBytes());
	}

	public String generateToken(String userName)
	{
		return generateToken(userName, new Date());
	}
	
	String generateToken(String userName, Date startDate)
	{
		return JWT.create().withIssuer("BasePagePlus").withSubject(userName)
				.withExpiresAt(Date.from(startDate.toInstant().plusSeconds(TOKEN_LENGTH))).sign(getAlgo());
		// Expires in 1 hour	
	}

	private static class JwtTokenExpiredException extends AuthenticationException
	{
		private static final long serialVersionUID = 1L;

		public JwtTokenExpiredException(String msg)
		{
			super(msg);
		}
	}

	public static class JwtAuthenticationToken implements Authentication
	{
		private static final long serialVersionUID = 1L;
		DecodedJWT decodedToken_;
		String token_;

		public JwtAuthenticationToken(String token, JwtUtils jwtUtils)
		{
			token_ = token;
			JWTVerifier verifier = JWT.require(jwtUtils.getAlgo()).build();
			try
			{
			  decodedToken_ = verifier.verify(token);
			}
			catch (TokenExpiredException tee)
			{
				throw new JwtTokenExpiredException("Your session has expired. Please log in again");
			}
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

		public boolean expiresSoon()
		{
			return Date.from(new Date().toInstant().plusSeconds(60 * 2)).after(decodedToken_.getExpiresAt());
		}
	}
}
