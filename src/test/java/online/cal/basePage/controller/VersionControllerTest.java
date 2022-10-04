package online.cal.basePage.controller;

import static org.junit.Assert.assertEquals;
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

@WebMvcTest(controllers = VersionController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class), properties = "app.version=0.0.0.0")

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration
public class VersionControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	VersionController versionController;

	@Test
	public void testVersionCall() throws Exception {
		this.mockMvc.perform(get("/api/v1/version")).andExpect(status().isOk()).andExpect(content().string("0.0.0.0"));

	}

	@Test
	public void testVersionFnc() {
		assertEquals("0.0.0.0", VersionController.getVersion());
	}

}
