package online.cal.basePage;

import java.security.*;
import java.time.*;
import java.util.*;

import org.bson.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.interfaces.*;
import com.mongodb.client.*;

import online.cal.basePage.model.*;

/**
 * JwtUtils, used to make a tokens for Websocket authentication
 */
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
				refreshAlgo();
			} catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return ALGO;
	}

	private static synchronized void refreshAlgo()
	{
		// If Server is started more than 2 days since a token secret was generated, generate
		// new random bits. Otherwise just use the bits in the database.
		MongoCollection<Document> collection = DBStore.getInstance().getCollection("JWTSecret");
		Document query = new Document("Purpose", "JWTSecret");
		
		Document d = DBStore.getInstance().findOne("JWTSecret", query);
		Date created = d == null ? null : d.getDate("created");
		byte secretBytes[];
		if (created == null || created.toInstant().plus(Period.ofDays(2)).isBefore(new Date().toInstant()))
		{
			collection.deleteMany(query);
		    SecureRandom random = new SecureRandom();
		    secretBytes = new byte[21];
		    random.nextBytes(secretBytes);
		    String b64 = Base64.getEncoder().encodeToString(secretBytes);
		    Document dbO = query.append("created", new Date()).append("jwtToken", b64);
		    collection.insertOne(dbO);
		}
		else
		{
			String b64 = d.getString("jwtToken");
			secretBytes = Base64.getDecoder().decode(b64);
		}
		ALGO =  Algorithm.HMAC256(secretBytes);
	}

	public static String generateToken(String userName)
	{
		return JWT.create().withIssuer("BasePagePlus").withSubject(userName)
				.withExpiresAt(Date.from(new Date().toInstant().plusSeconds(TOKEN_LENGTH))).sign(getAlgo());
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
		
		public boolean expiresSoon()
		{
			return Date.from(new Date().toInstant().plusSeconds(60 * 2)).after(decodedToken_.getExpiresAt());
		}
	}
}
