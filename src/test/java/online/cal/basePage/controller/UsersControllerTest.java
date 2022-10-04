package online.cal.basePage.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import online.cal.basePage.WebSecurityConfig;
import online.cal.basePage.model.BasePageUserService;
import online.cal.basePage.model.BasePageUser;

@WebMvcTest(controllers = UsersController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class))
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration
public class UsersControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	BasePageUserService userService_;

	@Test
	public void testUsers() throws Exception {
		List<BasePageUser> users = new ArrayList<>();
		users.add(new BasePageUser("William", ""));
		users.add(new BasePageUser("Kate", ""));
		users.add(new BasePageUser("Charles", ""));
		when(userService_.getUsers()).thenReturn(users);
		this.mockMvc.perform(get("/api/v1/users/")).andExpect(status().isOk()).andExpect(
				content().json("[{\"userName\":\"William\"}, {\"userName\":\"Kate\"}, {\"userName\":\"Charles\"}]"));

	}

}
