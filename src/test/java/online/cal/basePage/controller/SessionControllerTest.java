package online.cal.basePage.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import online.cal.basePage.WebSecurityConfig;
import online.cal.basePage.model.BasePageUserService;
import online.cal.basePage.testUtil.DeadStupidPrincipal;

@WebMvcTest(controllers = SessionController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class))

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration
public class SessionControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	AuthenticationManager authenticationManager;

	@MockBean
	BasePageUserService userService_;

	@BeforeEach
	public void setup() {
		Authentication authentication = new UsernamePasswordAuthenticationToken("loser", "password1");
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void test_username() throws Exception {
		this.mockMvc.perform(get("/api/v1/sessions/userName")).andExpect(status().isOk())
				.andExpect(content().string("loser"));
	}

	@Test
	public void test_init() throws Exception {
		this.mockMvc.perform(get("/api/v1/sessions/init")).andExpect(status().isOk())
				.andExpect(content().string("Welcome"));
	}

	@Test
	public void test_isLoggedIn() throws Exception {
		this.mockMvc.perform(get("/api/v1/sessions/isLoggedIn")).andExpect(status().isOk())
				.andExpect(content().string("true"));
	}

	@Test
	public void test_logout() throws Exception {
		MockHttpSession session = new MockHttpSession();
		this.mockMvc.perform(post("/api/v1/sessions/logout").session(session)).andExpect(status().isOk())
				.andExpect(content().json("{\"userName\":\"\",\"token\":\"\"}"))
				.andExpect(cookie().value("JSESSIONID", "deleted"));
		assertTrue(session.isInvalid());

		MockHttpSession unAuthedSession = Mockito.mock(MockHttpSession.class);
		Mockito.doThrow(new BadCredentialsException("invalidated")).when(unAuthedSession).invalidate();
		this.mockMvc.perform(post("/api/v1/sessions/logout").session(unAuthedSession))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json("{\"userName\":\"invalidated\",\"token\":\"\"}"));
	}

	@Test
	public void test_register() throws Exception {
		SessionController.LoginUser user = new SessionController.LoginUser();
		user.userName = "Bud";
		user.color = "Green";
		user.password = "frankenstein";
		String sUser = new ObjectMapper().writeValueAsString(user);
		MockHttpSession session = new MockHttpSession();
		BasePageUserService.UserMessage userMsg = new BasePageUserService.UserMessage("Bud", "token551");

		when(userService_.register(any())).thenReturn(userMsg);

		this.mockMvc
				.perform(post("/api/v1/sessions/register").session(session).content(sUser)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().json("{\"userName\":\"Bud\",\"token\":\"token551\"}"));
	}

	@Test
	public void test_registerFailure() throws Exception {
		SessionController.LoginUser user = new SessionController.LoginUser();
		user.userName = "Bud";
		user.color = "Green";
		user.password = "frankenstein";
		String sUser = new ObjectMapper().writeValueAsString(user);
		MockHttpSession session = new MockHttpSession();
		BasePageUserService.UserMessage userMsg = new BasePageUserService.UserMessage("Bud", "token551");

		Mockito.doThrow(new BadCredentialsException("invalidated")).when(userService_).register(any());

		this.mockMvc
				.perform(post("/api/v1/sessions/register").session(session).content(sUser)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json("{\"userName\":\"invalidated\",\"token\":\"\"}"));
	}

}
