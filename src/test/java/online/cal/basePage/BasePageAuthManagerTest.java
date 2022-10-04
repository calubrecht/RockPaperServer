package online.cal.basePage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import online.cal.basePage.model.BasePageUser;
import online.cal.basePage.model.BasePageUserService;
import online.cal.basePage.ws.AuthChannelInterceptorAdapter;

@SpringBootTest(classes = { BasePageAuthManager.class })
@ActiveProfiles({ "test" })
public class BasePageAuthManagerTest {

	@Autowired
	BasePageAuthManager manager;

	@MockBean
	BasePageUserService userService;

	@Test
	public void test_authenticate() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Bro", "Dude");
		when(userService.getUser("Bro"))
				.thenReturn(new BasePageUser("bro", "3ESAQ4Td0F6aOaVDStii2w==", "xBzvbPuOW3zihpRDvWpMHA=="));

		Authentication returned = manager.authenticate(token);

		assertEquals("Bro", returned.getName());
	}

	@Test
	public void test_authenticate_wrong_token() {
		RememberMeAuthenticationToken token = new RememberMeAuthenticationToken("abc", "Jane", null);

		try {
			manager.authenticate(token);
			fail("Authentcation should have failed");
		} catch (BadCredentialsException bce) {
			// expected
		}
		return;
	}

	@Test
	public void test_authenticate_wrong_pwd() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Bro", "Dudette");
		when(userService.getUser("Bro"))
				.thenReturn(new BasePageUser("bro", "3ESAQ4Td0F6aOaVDStii2w==", "xBzvbPuOW3zihpRDvWpMHA=="));

		try {
			manager.authenticate(token);
			fail("Authentcation should have failed");
		} catch (BadCredentialsException bce) {
			// expected
		}
		return;
	}

	@Test
	public void test_authenticate_unknown_user() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Bruce", "Dude");
		when(userService.getUser("Bro"))
				.thenReturn(new BasePageUser("bro", "3ESAQ4Td0F6aOaVDStii2w==", "xBzvbPuOW3zihpRDvWpMHA=="));

		try {
			manager.authenticate(token);
			fail("Authentcation should have failed");
		} catch (BadCredentialsException bce) {
			// expected
		}
		return;
	}

	@Test
	public void test_authenticate_guest() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Guest1", "");
		when(userService.getUser("Guest1")).thenReturn(new BasePageUser("Guest 1", ""));

		Authentication returned = manager.authenticate(token);

		assertEquals("Guest1", returned.getName());
	}

}
