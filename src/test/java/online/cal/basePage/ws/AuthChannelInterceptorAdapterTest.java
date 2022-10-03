package online.cal.basePage.ws;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import online.cal.basePage.JwtUtils;
import online.cal.basePage.model.BasePageUserService;

@SpringBootTest(classes = { AuthChannelInterceptorAdapter.class })
@ActiveProfiles({ "test" })
public class AuthChannelInterceptorAdapterTest {
	@Autowired
	AuthChannelInterceptorAdapter underTest;

	@MockBean
	WSSessionService sessionService_;

	@MockBean
	WSController wsController_;
	@MockBean
	BasePageUserService userService_;
	@MockBean
	JwtUtils jwtUtils_;

	@BeforeEach
	public void setupUpMocks() {
		byte[] someBytes = new byte[] { 69, -62, -17, -32, -37, -90, -125, 50, 80, -118, -69, 8, -83, -70, 95, 90, -55,
				24, -69, 115, 106 };
		when(jwtUtils_.getAlgo()).thenReturn(Algorithm.HMAC256(someBytes));
	}

	// From real JwtUtils
	String generateToken(String userName, Date startDate) {
		return JWT.create().withIssuer("BasePagePlus").withSubject(userName)
				.withExpiresAt(Date.from(startDate.toInstant().plusSeconds(60 * 60))).sign(jwtUtils_.getAlgo());
	}

	@Test
	public void testPreSend_connect_success() {
		String token = generateToken("loserNumber1", new Date());

		Map<String, List<String>> extHeaders = new HashMap<>();
		extHeaders.put("Authorization", Collections.singletonList("bearer=" + token));
		extHeaders.put("ClientSessionID", Collections.singletonList("clientSession1"));
		MessageHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT, extHeaders);
		accessor.setLeaveMutable(true);
		String payload = "something";
		Message<String> connectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());

		Message<?> resMessage = underTest.preSend(connectMessage, null);

		assertEquals("something", resMessage.getPayload().toString());
		UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) resMessage.getHeaders()
				.get("simpUser");
		assertEquals("loserNumber1", userToken.getPrincipal());

		verify(sessionService_, times(1)).addUserClientSession("loserNumber1", "clientSession1");
		verify(userService_, times(1)).onConnect("loserNumber1", "clientSession1");
		String authority = new ArrayList<>(userToken.getAuthorities()).get(0).getAuthority();
		assertEquals("USER", authority);
	}

	@Test
	public void testPreSend_connect_failed() {
		Map<String, List<String>> extHeaders = new HashMap<>();
		MessageHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT, extHeaders);
		accessor.setLeaveMutable(true);
		String payload = "something";
		Message<String> connectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());

		// No Token
		Message<?> resMessage = underTest.preSend(connectMessage, null);
		assertEquals(null, resMessage);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();
		String expiredToken = generateToken("loserNumber1", yesterday);
		extHeaders.put("Authorization", Collections.singletonList("bearer=" + expiredToken));

		accessor = StompHeaderAccessor.create(StompCommand.CONNECT, extHeaders);
		connectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());
		resMessage = underTest.preSend(connectMessage, null);
		assertEquals(null, resMessage);

		// Garbage token
		extHeaders.put("Authorization", Collections.singletonList("bearer=whatIsThis?"));
		accessor = StompHeaderAccessor.create(StompCommand.CONNECT, extHeaders);
		connectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());
		resMessage = underTest.preSend(connectMessage, null);
		assertEquals(null, resMessage);

		// Token with wrong byes
		byte[] diffBytes = new byte[] { 69, -62, -17, -32, -37, -90, -125, 50, 80, -118, -69, 8, -83, -70, 95, 90, -55,
				24, -69, 115, 96 };
		String badToken = JWT.create().withIssuer("BasePagePlus").withSubject("who")
				.withExpiresAt(Date.from(new Date().toInstant().plusSeconds(60 * 60)))
				.sign(Algorithm.HMAC256(diffBytes));
		extHeaders.put("Authorization", Collections.singletonList("bearer=" + badToken));

		accessor = StompHeaderAccessor.create(StompCommand.CONNECT, extHeaders);
		connectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());
		resMessage = underTest.preSend(connectMessage, null);
		assertEquals(null, resMessage);
	}

	@Test
	public void testPreSend_disconnect() {
		UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken("LeavingLoser", "");
		String token = generateToken("LeavingLoser", new Date());

		Map<String, List<String>> extHeaders = new HashMap<>();
		extHeaders.put("Authorization", Collections.singletonList("bearer=" + token));
		extHeaders.put("ClientSessionID", Collections.singletonList("clientSession12"));
		MessageHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT, extHeaders);
		accessor.setHeader("simpUser", userToken);
		accessor.setLeaveMutable(true);
		String payload = "something";
		Message<String> disconnectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());

		Message<?> resMessage = underTest.preSend(disconnectMessage, null);

		assertEquals("something", resMessage.getPayload().toString());
		verify(userService_, times(1)).onDisconnect("LeavingLoser", "clientSession12");

		// Disconnect with no user
		accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT, extHeaders);
		accessor.setLeaveMutable(true);
		disconnectMessage = new GenericMessage<>(payload, accessor.getMessageHeaders());
		resMessage = underTest.preSend(disconnectMessage, null);
		assertEquals("something", resMessage.getPayload().toString());
		verify(userService_, times(1)).onDisconnect(any(), any());

	}
}
