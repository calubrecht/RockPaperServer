package online.cal.basePage.model;

import static org.mockito.Mockito.*;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import online.cal.basePage.*;
import online.cal.basePage.model.BasePageUserService.*;

@RunWith(MockitoJUnitRunner.class)
public class BasePageUserServiceTest
{
	@InjectMocks
	BasePageUserService underTest_;

	@Mock
	DBStore dbStore_;

	@Mock
	ChatStore chatStore_;
	
	@Mock
	WebSecurityConfig securityService_;
	
	@After
	public void tearDown()
	{
		BasePageUserService.INSTANCE = null;
	}
	
	@BeforeClass
	public static void prepare()
	{
		BasePageUserService.INSTANCE = null;		
	}

	@Test
	public void testRegister()
	{
		// Do nothing token generator to mock out JwtUtils.generateToken
		underTest_.tokenGenerator_ = userName -> userName;
		BasePageUser user = new BasePageUser("FakeUser", "FakePass");
		user.setColor("puce");
		underTest_.register(user);
		
		assertTrue(underTest_.users_.containsKey("FakeUser"));
		
		assertEquals("puce", underTest_.users_.get("FakeUser").getColor());
		// Stored password should be hashed, not plaintext
		assertNotEquals("FakePass", underTest_.getUsers().get(0).getPassword());
		verify(dbStore_, times(1)).insertOne(eq("users"), any());
	}
	
	@Test
	public void testRegisterBadUsers()
	{
		String[] badUserNames = new String[] { "Guest 54", "#What", "AI - NOT" };
		for (String user : badUserNames)
		{
			try
			{
				underTest_.register(new BasePageUser(user, "pass"));
				fail(user + " should not be a valid username");
			} catch (BPUAuthenticationException e)
			{
				// expected
			}
		}
		BasePageUser user = new BasePageUser("FakeUser", "FakePass");
		underTest_.users_.put("FakeUser", user);
		
		try
		{
			underTest_.register(new BasePageUser("FakeUser", "pass"));
			fail("Should not be able to register user with existing name");
		} catch (BPUAuthenticationException e)
		{
			// expected
		}
	}
	
	@Test
	public void testOnConnect()
	{
		underTest_.users_.put("Franklin", new BasePageUser("Franklin", null));
		underTest_.users_.put("Wally",  new BasePageUser("Wally", null));
		List<BasePageUser> userMsgs = new ArrayList<>();
		underTest_.addListener(bpu -> userMsgs.add(bpu));
		
		underTest_.onConnect("Franklin", null);
		ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
		verify(chatStore_, times(1)).sendSystemMessage(msgCaptor.capture());
		
		assertEquals("Franklin has joined.", msgCaptor.getValue());
		assertEquals("CONNECTED", underTest_.getUser("Franklin").getStatus());
		assertEquals("Franklin", userMsgs.get(0).getUserName());

		underTest_.onConnect("Franklin", null);
		// Already connected, no message.
		assertEquals(1, msgCaptor.getAllValues().size());
		assertEquals("Franklin", userMsgs.get(1).getUserName());
	}
	
	@Test
	public void testOnConnectGuestUser()
	{
		List<BasePageUser> userMsgs = new ArrayList<>();
		underTest_.addListener(bpu -> userMsgs.add(bpu));
		underTest_.onConnect("Guest-3", null);
		ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
		verify(chatStore_, times(1)).sendSystemMessage(msgCaptor.capture());
		assertEquals("Guest-3 has joined.", msgCaptor.getValue());
		assertEquals("CONNECTED", underTest_.getUser("Guest-3").getStatus());
		assertEquals("Guest-3", userMsgs.get(0).getUserName());
		assertTrue( userMsgs.get(0).isGuest());
		assertEquals(3, underTest_.guestCount);
	}
	
	@Test
	public void testOnDisconnect()
	{
		underTest_.users_.put("Bob", new BasePageUser("Bob", null));
		underTest_.users_.put("Jo",  new BasePageUser("JO", null));
		underTest_.userStatuses_.put("Bob", "CONNECTED");
		underTest_.onDisconnect("Bob",  null);
		assertEquals("DISCONNECTING", underTest_.userStatuses_.get("Bob"));
		underTest_.onDisconnect("Bob",  null);
		assertEquals("DISCONNECTING", underTest_.userStatuses_.get("Bob"));
		underTest_.checkDisconnect("Bob", null);
		assertEquals("DISCONNECTED", underTest_.userStatuses_.get("Bob"));
		ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
		verify(chatStore_, times(1)).sendSystemMessage(msgCaptor.capture());
		assertEquals("Bob has left.", msgCaptor.getValue());
		
		underTest_.userStatuses_.put("Jo", "CONNECTED");
		underTest_.onDisconnect("Jo",  null);
		underTest_.onConnect("Jo",  null);
		underTest_.checkDisconnect("Jo", null);
		assertEquals("CONNECTED", underTest_.userStatuses_.get("Jo"));
		verify(chatStore_, times(1)).sendSystemMessage(any());
	}
	
	@Test
	public void testPasswordHash() throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasePageUser user = underTest_.generateUser("FakeUser", "FakePass");
		assertEquals("FakeUser", user.getUserName());
		assertFalse(user.validatePassword("NotPassword"));
		assertTrue(user.validatePassword("FakePass"));
	}

}
