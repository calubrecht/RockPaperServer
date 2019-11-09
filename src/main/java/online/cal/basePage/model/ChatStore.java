package online.cal.basePage.model;

import java.util.*;
import java.util.stream.*;

import javax.annotation.*;

import org.bson.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import com.mongodb.*;
import com.mongodb.client.*;

@Component("chatStore")
public class ChatStore
{	
	@Autowired
	DBStore dbStore_;
	
	public ChatStore()
	{

	}
	
	@PostConstruct
	public void init()
	{
	   MongoIterable<Document> itr = dbStore_.getCollection("chats").find().sort(new BasicDBObject("orderID", -1)).limit(50);
	   for (Document d : itr)
	   {
		   String user = d.getString("userName");
		   String message = d.getString("message");
		   long id = d.getLong("orderID");
		   addChat(new ChatMessage(user, message, id), false);
	   }
	}
	
	public void writeMsg(ChatMessage msg)
	{
		Document d =  new Document("orderID", msg.getMsgID()).append("userName", msg.getUserName()).append("message", msg.getChatText());
		dbStore_.getCollection("chats").insertOne(d);
	}
	
	/**
	 * Chat Store, replace with more permanent
	 */
	private List<ChatMessage> chatStore_ = new ArrayList<ChatMessage>();
	private List<ChatListener> listeners_ = new ArrayList<ChatListener>();
	
	long highestID_ = -1;
	public List<ChatMessage> getChatMessages()
	{
		return Collections.unmodifiableList(chatStore_);
	}
		
	public synchronized List<ChatMessage> getChatMessagesSince(Long id)
	{
		return getChatMessages().stream().filter(cm -> cm.getMsgID() > id).collect(Collectors.toList());
	}
	
	public synchronized void addChat(ChatMessage cm)
	{
		addChat(cm, true);
	}
	
	public synchronized void addChat(ChatMessage cm, boolean persist)
	{
		if (cm.getMsgID() == 0)
		{
    		highestID_++;
	    	cm.setMsgID(highestID_);
		}
		if (cm.getMsgID() > highestID_)
		{
			highestID_ = cm.getMsgID();
		}
		chatStore_.add(cm);
		if (persist)
		{
		  writeMsg(cm);
		}
		fireListeners(cm);
	}
	
	public synchronized void sendSystemMessage(String msg)
	{
		addChat(new ChatMessage("#system#", msg), false);
	}
	
	public void fireListeners(ChatMessage cm)
	{
		listeners_.forEach(l -> l.onChat(cm));
	}
	
	public void addListener(ChatListener listener)
	{
		listeners_.add(listener);
	}
	
	public static interface ChatListener
	{
		public void onChat(ChatMessage cm);
	}
}

