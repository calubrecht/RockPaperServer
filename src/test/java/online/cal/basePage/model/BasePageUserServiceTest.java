package online.cal.basePage.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bson.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import com.mongodb.client.*;

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
	
	@Mock
	JwtUtils jwtUtils;

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
		when(jwtUtils.generateToken(anyString())).thenReturn("token");
		underTest_.jwtUtils_ = jwtUtils;
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

		BasePageUser badPWUser = new BasePageUser("BadUser", null);
		try
		{
			underTest_.register(badPWUser);
			fail("Should not be able to register user with existing name");
		} catch (BPUAuthenticationException e)
		{
			// expected
		}
	}

	@Test
	public void testGetNonExistuser() {
		assertEquals(null, underTest_.getUser("ThisUserDoesn'tEvenExist"));
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

		underTest_.userStatuses_.put("Jacob", "DISCONNECTED");
		underTest_.onConnect("Jacob", null);
	    verify(chatStore_, times(2)).sendSystemMessage(msgCaptor.capture());

		assertEquals("Jacob has joined.", msgCaptor.getValue());
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
		
		underTest_.onConnect("Blrgl-poop", null);
		verify(chatStore_, times(2)).sendSystemMessage(msgCaptor.capture());
		assertEquals("Blrgl-poop has joined.", msgCaptor.getValue());
	}
	
	@Test
	public void testOnDisconnect() throws InterruptedException
	{
		underTest_.users_.put("Bob", new BasePageUser("Bob", null));
		underTest_.users_.put("Jo",  new BasePageUser("JO", null));
		underTest_.userStatuses_.put("Bob", "CONNECTED");
		underTest_.onDisconnect("Bob",  null);
		assertEquals("DISCONNECTING", underTest_.userStatuses_.get("Bob"));
		assertEquals("CONNECTED", underTest_.getUser("Bob").getStatus());
		underTest_.onDisconnect("Bob",  null);
		assertEquals("DISCONNECTING", underTest_.userStatuses_.get("Bob"));
		underTest_.checkDisconnect("Bob", null);
		assertEquals("DISCONNECTED", underTest_.userStatuses_.get("Bob"));
		assertEquals("DISCONNECTED", underTest_.getUser("Bob").getStatus());
		ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
		verify(chatStore_, times(1)).sendSystemMessage(msgCaptor.capture());
		assertEquals("Bob has left.", msgCaptor.getValue());
		underTest_.checkDisconnect("Bob", null);
		// Not called again
		verify(chatStore_, times(1)).sendSystemMessage(msgCaptor.capture());
		
		underTest_.userStatuses_.put("Jo", "CONNECTED");
		underTest_.onDisconnect("Jo",  null);
		underTest_.onConnect("Jo",  null);
		underTest_.checkDisconnect("Jo", null);
		assertEquals("CONNECTED", underTest_.userStatuses_.get("Jo"));
		verify(chatStore_, times(1)).sendSystemMessage(any());

		// onDisconnect calls onDisconnect after configured time
		int oldDisconnect = underTest_.disconnectWait;
		underTest_.disconnectWait = 5;
		underTest_.onConnect("Bradley",  null);
		underTest_.onDisconnect("Bradley", null);
		underTest_.disconnectWait = oldDisconnect;
		Thread.sleep(10);
		assertEquals("DISCONNECTED", underTest_.userStatuses_.get("Bradley"));
	}
	
	@Test
	public void testPasswordHash() throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasePageUser user = underTest_.generateUser("FakeUser", "FakePass");
		assertEquals("FakeUser", user.getUserName());
		assertFalse(user.validatePassword("NotPassword"));
		assertTrue(user.validatePassword("FakePass"));
	}

	private Document parse(String json)
	{
		Document doc = Document.parse(json);
		if (doc.containsKey("wins"))
		{
		  doc.put("wins", doc.getInteger("wins").longValue());
		}
		if (doc.containsKey("losses"))
		{
		  doc.put("losses", doc.getInteger("losses").longValue());
		}
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadUsers()
	{
		MongoCollection<Document> collection = Mockito.mock(MongoCollection.class);
		when(dbStore_.getCollection("users")).thenReturn(collection);
		FindIterable<Document> find = Mockito.mock(FindIterable.class);
		when(collection.find()).thenReturn(find);
		List<Document> userDocuments = new ArrayList<>();
		userDocuments.add(parse(
				"{\"userName\":\"Bobo\", \"passwordHash\":\"hashhash\", \"passwordSalt\":\"saltsalt\", \"wins\":5, \"losses\":12, \"color\":\"green\"}"));
		userDocuments.add(parse(
				"{\"userName\":\"Woody\", \"passwordHash\":\"hashhash\", \"passwordSalt\":\"saltsalt\", \"color\":\"taupe\"}"));
		Iterator<Document> itr = userDocuments.iterator();
		MongoCursor<Document> cursor = Mockito.mock(MongoCursor.class);
		when(find.iterator()).thenReturn(cursor);
		when(cursor.hasNext()).thenAnswer(i -> itr.hasNext());
		when(cursor.next()).thenAnswer(i -> itr.next());

		underTest_.init();

		assertEquals(5l, underTest_.getUser("Bobo").getWins());
		assertEquals(0l, underTest_.getUser("Woody").getWins());
	}
	
	
	@Test
	public void testCreateGuest()
	{
		assertEquals("Guest-1", underTest_.createGuest());
		assertEquals("Guest-2", underTest_.createGuest());
		
		assertTrue(underTest_.getUser("Guest-1").isGuest());
		assertTrue(underTest_.getUser("Guest-2").isGuest());
	}
	
	@Test
	public void testRecordRecords()
	{
		underTest_.users_.put("Bob", new BasePageUser("Bob", null));
		underTest_.users_.put("Jo",  new BasePageUser("Jo", null));
		underTest_.users_.put("Guest-1", new BasePageUser("Guest-1", null));
		
		List<BasePageUser> userMsgs = new ArrayList<>();
		underTest_.addListener(bpu -> userMsgs.add(bpu));
		// 	dbStore_.update("users", query, update);
		ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
		doNothing().when(dbStore_).update(eq("users"), any(), docCaptor.capture());
		
		underTest_.recordWin("Bob");
		underTest_.recordLoss("Jo");
		assertEquals(1l, docCaptor.getAllValues().get(0).get("wins"));
		assertEquals("Bob", userMsgs.get(0).getUserName());
		assertEquals(1l, userMsgs.get(0).getWins());
		assertEquals(0l, userMsgs.get(0).getLosses());
		
		assertEquals(1l, docCaptor.getAllValues().get(1).get("losses"));
		assertEquals("Jo", userMsgs.get(1).getUserName());
		assertEquals(0l, userMsgs.get(1).getWins());
		assertEquals(1l, userMsgs.get(1).getLosses());
		
		underTest_.recordWin("AI - 1");
		underTest_.recordLoss("AI - 1");
		assertEquals(2, docCaptor.getAllValues().size());
		assertEquals(2, userMsgs.size());
		
		underTest_.recordWin("Guest-1");
		underTest_.recordLoss("Guest-1");
		assertEquals(2, docCaptor.getAllValues().size());
		assertEquals("Guest-1", userMsgs.get(2).getUserName());
		assertEquals(1l, userMsgs.get(2).getWins());
		assertEquals(0l, userMsgs.get(2).getLosses());
		assertEquals("Guest-1", userMsgs.get(3).getUserName());
		assertEquals(1l, userMsgs.get(3).getWins());
		assertEquals(1l, userMsgs.get(3).getLosses());
	}
}
