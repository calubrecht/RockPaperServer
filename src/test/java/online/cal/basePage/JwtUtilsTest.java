package online.cal.basePage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.time.temporal.*;
import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;

import online.cal.basePage.model.*;

@RunWith(MockitoJUnitRunner.class)
public class JwtUtilsTest
{
	byte[] notRandomBytes = new byte[] {1 ,2, 3, 4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};

	@InjectMocks
	JwtUtils underTest;
	
	@Mock
	JwtByteSource byteSource;
	
	@Test
	public void testGenerateToken() {
		when(byteSource.getBytes()).thenReturn(notRandomBytes);
		
		String sToken = underTest.generateToken("Frank");
		
		JwtUtils.JwtAuthenticationToken token = new JwtUtils.JwtAuthenticationToken(sToken, underTest);
		assertEquals("Frank", token.getName());
		assertEquals("Frank", token.getPrincipal());
		assertFalse(token.expiresSoon());
		
		
	}
	
	@Test
	public void testTokenExpiry()
	{
	    when(byteSource.getBytes()).thenReturn(notRandomBytes);
		Date hourAgo = Date.from(new Date().toInstant().minus(1, ChronoUnit.HOURS).minusSeconds(1));
		String sToken = underTest.generateToken("Frank", hourAgo);
		try
		{
		  JwtUtils.JwtAuthenticationToken token = new JwtUtils.JwtAuthenticationToken(sToken, underTest);
          fail("Token should have expired");
		}
		catch(AuthenticationException e)
		{
			
		}
		Date fiftyEightMinutes = Date.from(new Date().toInstant().minus(58, ChronoUnit.MINUTES));
		String nearlyExpired = underTest.generateToken("Frank", fiftyEightMinutes);
		JwtUtils.JwtAuthenticationToken token = new JwtUtils.JwtAuthenticationToken(nearlyExpired, underTest);
		assertTrue(token.expiresSoon());
		
		JwtUtils.JwtAuthenticationToken freshToken = new JwtUtils.JwtAuthenticationToken(underTest.generateToken("Frank"), underTest);
		assertFalse(freshToken.expiresSoon());
	}
	
	@Test
	public void testToken()
	{
		when(byteSource.getBytes()).thenReturn(notRandomBytes);
		JwtUtils.JwtAuthenticationToken token = new JwtUtils.JwtAuthenticationToken(underTest.generateToken("Bob"),
				underTest);

		assertEquals(1, token.getAuthorities().size());
		assertTrue(token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
		assertEquals(null, token.getCredentials());
		assertEquals(null, token.getDetails());
		assertTrue(token.isAuthenticated());
		try
		{
			token.setAuthenticated(true);
			fail("Shouldn't be able to manually set authenticated");
		} catch (IllegalArgumentException e)
		{

		}
	}
}
