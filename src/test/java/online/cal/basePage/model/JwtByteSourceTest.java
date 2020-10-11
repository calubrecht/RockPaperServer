package online.cal.basePage.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

import java.text.*;
import java.time.temporal.*;
import java.util.*;

import org.bson.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import com.mongodb.client.*;

@RunWith(MockitoJUnitRunner.class)
public class JwtByteSourceTest
{
	@InjectMocks
	JwtByteSource underTest;
	
	@Mock
	DBStore dbStore;
	
	static final SimpleDateFormat MONGO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private String fmtDate(Date d)
	{
		return MONGO_DATE_FORMAT.format(d);
	}
	
	private Document makeDoc(String token, Date date)
	{
		Map<String, Object> document = new HashMap<>();
		document.put("jwtToken", token);
		document.put("created", date);
		return new Document(document);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetBytesFresh()
	{
		MongoCollection<Document> collection = Mockito.mock(MongoCollection.class);
		when(dbStore.getCollection(anyString())).thenReturn(collection);
		when(dbStore.findOne(anyString(), any())).thenReturn(makeDoc("rO15R/HLqAh9c6ani4BLgPOv7+JZ", new Date()));
		
		byte[] bytes = underTest.getBytes();
		assertNotEquals(null, bytes);
		assertEquals(21, bytes.length);
		verify(collection, never()).insertOne(any());
		verify(collection, never()).deleteMany(any());
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetBytesExpired()
	{
		MongoCollection<Document> collection = Mockito.mock(MongoCollection.class);
		when(dbStore.getCollection(anyString())).thenReturn(collection);
		Date oldDate = Date.from(new Date().toInstant().minus(2, ChronoUnit.DAYS).minusSeconds(5));
		when(dbStore.findOne(anyString(), any())).thenReturn(makeDoc("rO15R/HLqAh9c6ani4BLgPOv7+JZ", oldDate));
		
		byte[] bytes = underTest.getBytes();
		assertNotEquals(null, bytes);
		assertEquals(21, bytes.length);
		verify(collection, times(1)).insertOne(any());
		verify(collection, times(1)).deleteMany(any());
	}
}
