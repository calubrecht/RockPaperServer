package online.cal.basePage.ws;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WSSessionServiceTest {

	WSSessionService underTest;

	@BeforeEach
	public void setup() {
		underTest = new WSSessionService();
	}

	@Test
	public void testAddGetSessions() {
		underTest.addUserClientSession("someUser", "thatSession");

		assertEquals(Arrays.asList("thatSession"), underTest.getSessionIDs("someUser"));

		underTest.addUserClientSession("someUser", "thatOtherSession");
		assertEquals(Arrays.asList("thatSession", "thatOtherSession"), underTest.getSessionIDs("someUser"));

		underTest.clearUser("someUser");
		assertEquals(Collections.emptyList(), underTest.getSessionIDs("someUser"));
	}

}
