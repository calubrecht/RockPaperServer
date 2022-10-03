package online.cal.basePage.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import online.cal.basePage.WebSecurityConfig;
import online.cal.basePage.model.GameMessage;
import online.cal.basePage.model.GameService;
import online.cal.basePage.model.Pair;
import online.cal.basePage.testUtil.DeadStupidPrincipal;

@WebMvcTest(controllers = GameController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class))

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration
public class GameControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private GameService gameService_;

	ObjectMapper jsonMapper = new ObjectMapper();

	@Test
	public void testSeekGame() throws Exception {
		Principal princ = new DeadStupidPrincipal("loser");
		;
		this.mockMvc.perform(post("/api/v1/game/seek").principal(princ)).andExpect(status().isOk()).andExpect(content()
				.json("{\"id\":\"thisNewGame\", \"action\":\"pending\", \"detail\":\"\",\"players\":[\"loser\",\"loser\"]}"));

		verify(gameService_, times(1)).seekGame("loser");
	}

	@Test
	public void testEndSeek() throws Exception {
		Principal princ = new DeadStupidPrincipal("loser");
		;
		this.mockMvc.perform(post("/api/v1/game/endSeek").principal(princ)).andExpect(status().isOk())
				.andExpect(content().json(
						"{\"id\":\"thisNewGame\", \"action\":\"pending\", \"detail\":\"\",\"players\":[\"loser\",\"loser\"]}"));

		verify(gameService_, times(1)).endSeekGame("loser");
	}

	@Test
	public void testStartAIGame() throws Exception {
		Principal princ = new DeadStupidPrincipal("loser");
		;
		this.mockMvc.perform(post("/api/v1/game/startAIGame").principal(princ)).andExpect(status().isOk())
				.andExpect(content().json(
						"{\"id\":\"thisNewGame\", \"action\":\"pending\", \"detail\":\"\",\"players\":[\"loser\",\"loser\"]}"));

		verify(gameService_, times(1)).startAIGame("loser");
	}

	@Test
	public void testResendActive() throws Exception {
		Principal princ = new DeadStupidPrincipal("loser");
		;
		this.mockMvc.perform(post("/api/v1/game/resendActive").principal(princ)).andExpect(status().isOk());

		verify(gameService_, times(1)).resendActive("loser");
	}

	@Test
	public void testCancelGame() throws Exception {
		Mockito.doThrow(new GameService.InvalidActionException("What")).when(gameService_).cancelGame("canceled");
		Principal princ = new DeadStupidPrincipal("loser");
		String body = jsonMapper.writeValueAsString(new GameMessage("5511", "cancelGame", "", null));
		this.mockMvc
				.perform(post("/api/v1/game/cancel").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().json(
						"{\"id\":\"5511\", \"action\":\"canceled\", \"detail\":\"\",\"players\":[\"loser\",\"loser\"]}"));

		verify(gameService_, times(1)).cancelGame("5511");
		String body2 = jsonMapper.writeValueAsString(new GameMessage("canceled", "cancelGame", "", null));
		this.mockMvc
				.perform(post("/api/v1/game/cancel").principal(princ).content(body2)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().json(
						"{\"id\":\"canceled\", \"action\":\"canceled\", \"detail\":\"Game already canceled\",\"players\":[\"loser\",\"loser\"]}"));

		verify(gameService_, times(1)).cancelGame("canceled");
	}

	@Test
	public void testInvite() throws Exception {
		Mockito.doThrow(new GameService.InvalidActionException("Cannot invite a player while in a game."))
				.when(gameService_).inviteGame("loser", "playerInGame");
		Principal princ = new DeadStupidPrincipal("loser");
		String body = jsonMapper.writeValueAsString(new GameMessage("", "", "", new Pair<String>("loser", "weirdo")));
		this.mockMvc
				.perform(post("/api/v1/game/invite").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content()
						.json("{\"id\":\"\", \"action\":\"ok\", \"detail\":\"\",\"players\":[\"loser\",\"weirdo\"]}"));
		verify(gameService_, times(1)).inviteGame("loser", "weirdo");

		body = jsonMapper.writeValueAsString(new GameMessage("", "", "", new Pair<String>("loser", "playerInGame")));
		this.mockMvc
				.perform(post("/api/v1/game/invite").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().json(
						"{\"id\":\"\", \"action\":\"error\", \"detail\":\"Cannot invite a player while in a game.\",\"players\":[\"loser\",\"playerInGame\"]}"));
		verify(gameService_, times(1)).inviteGame("loser", "playerInGame");
		verify(gameService_, times(2)).inviteGame(any(), any());

		// Can't invite with no players
		body = jsonMapper.writeValueAsString(new GameMessage("", "", "", null));
		this.mockMvc
				.perform(post("/api/v1/game/invite").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("{\"id\":\"\", \"action\":\"error\", \"detail\":\"Invalid players list\"}"));

		// Can't invite yourself
		body = jsonMapper.writeValueAsString(new GameMessage("", "", "", new Pair<String>("loser", "loser")));
		this.mockMvc
				.perform(post("/api/v1/game/invite").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().json(
						"{\"id\":\"\", \"action\":\"error\", \"detail\":\"Invalid players list\",\"players\":[\"loser\",\"loser\"]}"));

		// Can't invite on someone else;s behalf
		body = jsonMapper.writeValueAsString(new GameMessage("", "", "", new Pair<String>("weirdo", "who")));
		this.mockMvc
				.perform(post("/api/v1/game/invite").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().json(
						"{\"id\":\"\", \"action\":\"error\", \"detail\":\"Invalid players list\",\"players\":[\"weirdo\",\"who\"]}"));
		// None of these called invitegame at all
		verify(gameService_, times(2)).inviteGame(any(), any());
	}

	@Test
	public void testAccept() throws Exception {
		Principal princ = new DeadStupidPrincipal("loser");
		String body = jsonMapper
				.writeValueAsString(new GameMessage("acceptThis", "", "", new Pair<String>("loser", "weirdo")));
		this.mockMvc
				.perform(post("/api/v1/game/acceptInvite").principal(princ).content(body)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("{\"id\":\"\", \"action\":\"ok\", \"detail\":\"\"}"));
		verify(gameService_, times(1)).acceptInvite("acceptThis");
	}

}
