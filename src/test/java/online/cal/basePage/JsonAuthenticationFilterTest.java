package online.cal.basePage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import online.cal.basePage.model.BasePageUserService;

public class JsonAuthenticationFilterTest {

	@Test
	public void test_attemptAuthentication() throws AuthenticationException, IOException, ServletException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("POST");
		String body = "{\"userName\":\"Bob\", \"password\":\"thePassword\"}";
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

		AuthenticationManager mgr = Mockito.mock(AuthenticationManager.class);
		when(mgr.authenticate(any())).thenAnswer(a -> {
			return a.getArgument(0);
		});
		JwtUtils jwtUtils = Mockito.mock(JwtUtils.class);
		JsonAuthenticationFilter filter = new JsonAuthenticationFilter("/", mgr, jwtUtils);
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) filter
				.attemptAuthentication(request, null);

		assertEquals(request, authentication.getDetails());
		assertEquals("Bob", authentication.getName());
		assertEquals("thePassword", authentication.getCredentials());
	}

	@Test
	public void test_attemptAuthorizationRequiresPost() throws IOException, ServletException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("GET");
		JsonAuthenticationFilter filter = new JsonAuthenticationFilter("/", null, null);
		try {
			filter.attemptAuthentication(request, null);
			fail("Should have thrown if not POST");

		} catch (AuthenticationException ae) {
			// Expected
		}

	}

	@Test
	public void testGuestAuthFilter() throws IOException, AuthenticationException, ServletException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("POST");
		String body = "{\"userName\":\"Bob\", \"password\":\"thePassword\"}";
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

		AuthenticationManager mgr = Mockito.mock(AuthenticationManager.class);
		when(mgr.authenticate(any())).thenAnswer(a -> {
			return a.getArgument(0);
		});
		BasePageUserService userService = Mockito.mock(BasePageUserService.class);
		when(userService.createGuest()).thenReturn("Guest 55");
		JsonAuthenticationFilter filter = new JsonAuthenticationFilter.GuestAuthFilter("/", mgr, userService, null);
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) filter
				.attemptAuthentication(request, null);

		assertEquals(request, authentication.getDetails());
		assertEquals("Guest 55", authentication.getName());
		assertEquals("", authentication.getCredentials());

	}

	@Test
	public void test_successHandler() throws IOException, ServletException {
		JwtUtils jwtUtils = Mockito.mock(JwtUtils.class);
		when(jwtUtils.generateToken(any())).thenReturn("aToken!");
		JsonAuthenticationFilter.JsonSuccessHandler handler = new JsonAuthenticationFilter.JsonSuccessHandler(jwtUtils);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		StringWriter writer = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("Jimmy", "");
		handler.onAuthenticationSuccess(null, response, authentication);
		String output = writer.toString();
		assertEquals("{\"userName\": \"Jimmy\", \"token\":\"aToken!\"}", output);

	}

}
