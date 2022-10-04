package online.cal.basePage.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.time.temporal.*;
import java.util.*;

import org.bson.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import com.mongodb.client.*;

@RunWith(MockitoJUnitRunner.class)
public class ChatStoreTest
{

	@InjectMocks
	ChatStore store_;
	
	@Mock
	DBStore dbStore;
	
	@Mock
    MongoCollection<Document> collection;
	
	@Before
	public void setup()
	{
		when(dbStore.getCollection("chats")).thenReturn(collection);
	}
	
	@Test
	public void testListeners()
	{
		List<ChatMessage> msgs = new ArrayList<ChatMessage>();
		store_.addListener(msg -> msgs.add(msg));
		
		store_.addChat(new ChatMessage("Bob", "Not a system"));
		store_.sendSystemMessage("This is a system message");
		
		assertEquals(2, msgs.size());
		assertEquals("Bob", msgs.get(0).getUserName());
		assertEquals(0l, msgs.get(0).getMsgID());
		assertEquals("#system#", msgs.get(1).getUserName());
		assertEquals(1l, msgs.get(1).getMsgID());
	}

	@Test
	public void testAddChatPersistance()
	{
      store_.addChat(new ChatMessage("Frank", "I like turtles"));
      
      verify(collection, times(1)).insertOne(any(Document.class));
      
      store_.addChat(new ChatMessage("Sis", "This is a system message"), false);
      verify(collection, times(1)).insertOne(any(Document.class));
	}
	
	private Document parse(String json)
	{
		Document doc = Document.parse(json);
		doc.put("orderID", doc.getInteger("orderID").longValue());
		return doc;
	}
	
	@Test
	public void testLoadDBMsgs()
	{
       @SuppressWarnings("unchecked")
	   FindIterable<Document> find = Mockito.mock(FindIterable.class);
       when(collection.find()).thenReturn(find);
       when(find.sort(any())).thenReturn(find);
       when(find.limit(anyInt())).thenReturn(find);
       List<Document> chatDocuments = new ArrayList<Document>();
       chatDocuments.add(parse("{\"userName\":\"Frank\",\"message\":\"Long long way from home\", \"orderID\":10}"));
       chatDocuments.add(parse("{\"userName\":\"Joe\",\"message\":\"Where?\", \"orderID\":5}"));
       chatDocuments.add(parse("{\"userName\":\"Frank\",\"message\":\"I'm lost.\", \"orderID\":4}"));
       chatDocuments.add(parse("{\"userName\":\"Mary\",\"message\":\"Nowhere\", \"orderID\":3}"));
       Iterator<Document> itr = chatDocuments.iterator();
       @SuppressWarnings("unchecked")
	   MongoCursor<Document> cursor = Mockito.mock(MongoCursor.class);
       when(find.iterator()).thenReturn(cursor);
       when(cursor.hasNext()).thenAnswer(i -> itr.hasNext());
       when(cursor.next()).thenAnswer(i -> itr.next());
       
       store_.loadDBMessages();
       List<ChatMessage> msgs = store_.getChatMessages();
       assertEquals(4, msgs.size());
       assertEquals("Frank", msgs.get(0).getUserName());
       assertEquals("Long long way from home", msgs.get(0).getChatText());
       assertEquals("Mary", msgs.get(3).getUserName());
       assertEquals("Nowhere", msgs.get(3).getChatText());
       
	}
	
	@Test
	public void testInit() throws InterruptedException {
		store_.pollTime = 10; // Stupid fast
		@SuppressWarnings("unchecked")
		FindIterable<Document> find = Mockito.mock(FindIterable.class);
		when(collection.find()).thenReturn(find);
		when(find.sort(any())).thenReturn(find);
		when(find.limit(anyInt())).thenReturn(find);
		Iterator<Document> itr = Collections.emptyIterator();
		@SuppressWarnings("unchecked")
		MongoCursor<Document> cursor = Mockito.mock(MongoCursor.class);
		when(find.iterator()).thenReturn(cursor);
		when(cursor.hasNext()).thenAnswer(i -> itr.hasNext());

		store_.init();
		verify(dbStore, times(1)).getCollection(any());

		ChatMessage oldSystemMessage = new ChatMessage(ChatMessage.SYSTEM, "Too Old");
		Date sixtyTwoMinutesAgo = Date.from(new Date().toInstant().minus(62, ChronoUnit.MINUTES));
		oldSystemMessage.dt_ = sixtyTwoMinutesAgo;
		store_.addChat(oldSystemMessage);
		for (int counter = 0; counter < 50; counter++) {
			Thread.sleep(10);
			List<ChatMessage> msgs = store_.getChatMessages();
			if (msgs.size() == 0) {
				return;
			}
		}
		fail("ChatStore did not clean up old system messages in .5s");
	}

	@Test
	public void testGetChatMessagesSince()
	{
	  store_.addChat(new ChatMessage("Frank", "MEssage1"));
	  store_.addChat(new ChatMessage("Frank", "MEssage2"));
	  store_.addChat(new ChatMessage("Amanda", "What?"));
	  store_.addChat(new ChatMessage("Bob", "Other Message"));
	  
	  
	  List<ChatMessage> msgs = store_.getChatMessagesSince(1l);
	  
	  assertEquals(2, msgs.size());
	  assertEquals("What?", msgs.get(0).getChatText());
	  assertEquals("Other Message", msgs.get(1).getChatText());
	}
	
	@Test
	public void testCullOldSystemMessages()
	{
		Date sixtyTwoMinutesAgo = Date.from(new Date().toInstant().minus(62, ChronoUnit.MINUTES));
		ChatMessage oldSystemMessage = new ChatMessage(ChatMessage.SYSTEM, "Too Old");
		oldSystemMessage.dt_ = sixtyTwoMinutesAgo;
		ChatMessage newSystemMessage = new ChatMessage(ChatMessage.SYSTEM, "Not Too Old");
		ChatMessage oldRealMessage = new ChatMessage("Frank", "Old but who careas");
		oldRealMessage.dt_ = sixtyTwoMinutesAgo;
		
		store_.addChat(oldSystemMessage);
		store_.addChat(newSystemMessage);
		store_.addChat(oldRealMessage);
		
		store_.cullOldSystemMessages();
		List<ChatMessage> msgs = store_.getChatMessages();
		assertEquals(2, msgs.size());
		assertEquals("Not Too Old", msgs.get(0).getChatText());
		assertEquals("Old but who careas", msgs.get(1).getChatText());
	}

	@Test
	public void testChatMessage() {
		ChatMessage cm = new ChatMessage();
		cm.setChatText("Hallo");
		cm.setMsgID(5l);
		cm.setUserName("Matt");

		assertEquals("Hallo", cm.getChatText());
		assertEquals(5l, cm.getMsgID());
		assertEquals("Matt", cm.getUserName());
	}
}
