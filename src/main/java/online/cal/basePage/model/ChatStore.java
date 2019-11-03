package online.cal.basePage.model;

import java.util.*;
import java.util.stream.*;

public class ChatStore
{
	static ChatStore instance_ = new ChatStore();
	
	public static ChatStore getInstance()
	{
		return instance_;
	}
	
	public ChatStore()
	{
		ChatMessage[] messages = new ChatMessage[]{
				new ChatMessage("#system#", "<luser> has entered"),
				new ChatMessage("luser", "Hey all!"),
				new ChatMessage("#system#", "<bozo> has entered"),
				new ChatMessage("luser", "Hey, bozo"),
				new ChatMessage("bozo", "Hey, l. Want a game?"),
				new ChatMessage("#system#", "<spongey> has entered"),
				new ChatMessage("#system#", "<spongey> has left"),
				new ChatMessage("#system#", "<bozo> has invited you to a game")};
	   for (ChatMessage cm : messages)
	   {
		   addChat(cm);
	   }
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
		highestID_++;
		cm.setMsgID(highestID_);
		chatStore_.add(cm);
		fireListeners(cm);
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

