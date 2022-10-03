package online.cal.basePage.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import online.cal.basePage.WebSecurityConfig;

@WebMvcTest(controllers = SimpleController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class))

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration
public class SimpleControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	public void testSimpleGets() throws Exception {
		this.mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string("Boot!"));
		this.mockMvc.perform(get("/lyrics")).andExpect(status().isOk()).andExpect(content().string(
				"<html><head><title>Boot</title></head><body><p>Give 'em the <b style='color:#ff0000'>BOOT</b>! The roots, the radicals</p></body></html>"));
		this.mockMvc.perform(get("/test")).andExpect(status().isOk()).andExpect(
				content().string("<html><head><title>Boot</title></head><body><p>Should see this</p></body></html>"));
	}

	@Test
	public void testError() throws Exception {
		this.mockMvc.perform(get("/error")).andExpect(status().isOk()).andExpect(content().string("error"));
		this.mockMvc.perform(get("/error").requestAttr("javax.servlet.error.status_code", 404))
				.andExpect(status().isOk()).andExpect(content().string("error-404"));
		this.mockMvc.perform(get("/error").requestAttr("javax.servlet.error.status_code", 500))
				.andExpect(status().isOk()).andExpect(content().string("error-500"));
		this.mockMvc.perform(get("/error").requestAttr("javax.servlet.error.status_code", 401))
				.andExpect(status().isOk()).andExpect(content().string("Unable to authenticate. Please log in again"));
		this.mockMvc.perform(get("/error").requestAttr("javax.servlet.error.status_code", 403))
				.andExpect(status().isOk()).andExpect(content().string("Unable to authenticate. Please log in again"));
		this.mockMvc.perform(get("/error").requestAttr("javax.servlet.error.status_code", 406))
				.andExpect(status().isOk()).andExpect(content().string("error-406"));
	}
}
