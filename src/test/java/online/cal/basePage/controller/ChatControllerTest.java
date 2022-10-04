package online.cal.basePage.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import online.cal.basePage.WebSecurityConfig;
import online.cal.basePage.model.ChatMessage;
import online.cal.basePage.model.ChatStore;
import online.cal.basePage.model.GameMessage;

@WebMvcTest(controllers = ChatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class))

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration
public class ChatControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private ChatStore chatStore;

	ObjectMapper jsonMapper = new ObjectMapper();

	@Test
	public void testGetChats() throws Exception {
		when(chatStore.getChatMessages())
				.thenReturn(Arrays.asList(new ChatMessage("Brian", "Hi"), new ChatMessage("Jolene", "Hi back")));
		this.mockMvc.perform(get("/api/v1/chat/chats")).andExpect(status().isOk()).andExpect(content().json(
				"[{\"userName\":\"Brian\", \"chatText\":\"Hi\"},{\"userName\":\"Jolene\", \"chatText\":\"Hi back\"}]"));

	}

	@Test
	public void testGetChatsSince() throws Exception {
		when(chatStore.getChatMessagesSince(23l)).thenReturn(
				Arrays.asList(new ChatMessage("Brainn", "Hi", 28), new ChatMessage("Jolene", "Hi back", 32)));
		this.mockMvc.perform(get("/api/v1/chat/chatsSince/23")).andExpect(status().isOk()).andExpect(content().json(
				"{\"mostRecentData\":32,\"data\":[{\"userName\":\"Brainn\",\"msgID\":28,\"chatText\":\"Hi\"},{\"userName\":\"Jolene\",\"msgID\":32,\"chatText\":\"Hi back\"}]}"));

	}

	@Test
	public void testGetNoChatsSince() throws Exception {
		when(chatStore.getChatMessagesSince(23l)).thenReturn(Collections.emptyList());
		this.mockMvc.perform(get("/api/v1/chat/chatsSince/23")).andExpect(status().isOk())
				.andExpect(content().json("{\"mostRecentData\":23,\"data\":[]}"));

	}

	@Test
	public void testAddChat() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken("loser", "password1");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String body = jsonMapper.writeValueAsString(new ChatMessage("Leo", "Donnie does machines"));
		this.mockMvc.perform(post("/api/v1/chat/chat").content(body).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("{\"userName\":\"loser\", \"chatText\":\"Donnie does machines\"}"));
		ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
		verify(chatStore, times(1)).addChat(captor.capture());

		assertEquals("loser", captor.getValue().getUserName());
		assertEquals("Donnie does machines", captor.getValue().getChatText());
	}

}
