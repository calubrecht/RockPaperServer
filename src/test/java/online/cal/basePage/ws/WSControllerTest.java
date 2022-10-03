package online.cal.basePage.ws;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import online.cal.basePage.model.BasePageUser;
import online.cal.basePage.model.BasePageUserService;
import online.cal.basePage.model.ChatMessage;
import online.cal.basePage.model.ChatStore;
import online.cal.basePage.model.GameMessage;
import online.cal.basePage.model.GameService;
import online.cal.basePage.model.GameService.InvalidActionException;
import online.cal.basePage.model.Pair;
import online.cal.basePage.testUtil.DeadStupidPrincipal;

@SpringBootTest(classes = { WSController.class })
@ActiveProfiles({ "test" })
public class WSControllerTest {

	@Autowired
	WSController wsController;

	@MockBean
	ChatStore store_;

	@MockBean
	GameService gameService_;

	@MockBean
	BasePageUserService userService_;

	@MockBean
	WSSessionService sessionService_;

	@MockBean
	SimpMessagingTemplate messagingTemplate_;

	@Test
	public void test_onReceivedGameMessage() throws InvalidActionException {
		Principal p = new DeadStupidPrincipal("Josh");
		GameMessage msg = new GameMessage("id11", "makeChoice", "Frog", null);

		wsController.onReceivedGameMessage(p, null, msg);

		verify(gameService_, times(1)).makeChoice(any(), any(), eq("Frog"));

		// If gameService throws, send reply message
		Mockito.doThrow(new GameService.InvalidActionException("Only frogs")).when(gameService_).makeChoice(any(),
				any(), eq("Toad"));
		when(sessionService_.getSessionIDs(any())).thenReturn(Collections.singletonList("Josh's Session"));
		GameMessage toadMsg = new GameMessage("id11", "makeChoice", "Toad", null);

		wsController.onReceivedGameMessage(p, null, toadMsg);
		ArgumentCaptor<GameMessage> captor = ArgumentCaptor.forClass(GameMessage.class);
		verify(messagingTemplate_, times(1)).convertAndSend(eq("/queue/game-Josh's Session"), captor.capture());
		GameMessage replyMessage = captor.getValue();

		assertEquals("id11", replyMessage.getGameID());
		assertEquals("ERROR", replyMessage.getAction());
		assertEquals("Only frogs", replyMessage.getDetail());

		// Accept and safely ignore message for unnknown action
		GameMessage unknownMsg = new GameMessage("id11", "fryChicken", "Extra Crispy", null);
		wsController.onReceivedGameMessage(p, null, unknownMsg);
	}

	@Test
	public void test_onChat() {
		ChatMessage cm = new ChatMessage("Gronk", "I'm gronk");
		wsController.onChat(cm);

		verify(messagingTemplate_).convertAndSend("/topic/chat", cm);
	}

	@Test
	public void test_onUser() {
		BasePageUser user = new BasePageUser("Gronk", "");
		wsController.onUser(user);

		verify(messagingTemplate_).convertAndSend("/topic/users", user);
	}

	@Test
	public void testOnUserSubscribe() {
		when(userService_.getUser(eq("Marigold"))).thenReturn(new BasePageUser("Marigold", ""));
		Map<String, Object> headers = new HashMap<>();
		headers.put("simpUser", new UsernamePasswordAuthenticationToken("Marigold", ""));
		BasePageUser user = wsController.onUserSubscribe(headers);
		assertEquals("Marigold", user.getUserName());

		// No user header = no user
		headers = new HashMap<>();
		user = wsController.onUserSubscribe(headers);
		assertEquals(null, user);
	}

}
