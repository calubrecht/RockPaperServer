package online.cal.basePage.model;

import java.util.*;
import java.util.stream.*;

import com.fasterxml.jackson.annotation.*;

public class ChatMessage
{
    @JsonProperty("userName") private String userName_;
    @JsonProperty("chatText") private String chatText_;
    @JsonProperty("msgID") private long msgID_;
    
  /*  public ChatMessage()
    {
    	
    }*/
    
    public ChatMessage(String userName, String chatText)
    {
    	userName_ = userName;
    	chatText_ = chatText;
    }

	public String getUserName()
	{
		return userName_;
	}

	public void setUserName(String userName)
	{
		userName_ = userName;
	}

	public String getChatText()
	{
		return chatText_;
	}

	public void setChatText(String chatText)
	{
		chatText_ = chatText;
	}
	
	public long getMsgID()
	{
		return msgID_;
	}

	protected void setMsgID(long index)
	{
		msgID_ = index;
	}
	
	/**
	 * Chat Store, replace with more permanent
	 */
	private static List<ChatMessage> chatStore_ = new ArrayList<ChatMessage>();
	public static List<ChatMessage> getChatMessages()
	{
		return Collections.unmodifiableList(chatStore_);
	}
	
	public static List<ChatMessage> getChatMessagesSince(Long id)
	{
		return getChatMessages().stream().filter(cm -> cm.getMsgID() > id).collect(Collectors.toList());
	}
	
	public synchronized static void addChat(ChatMessage cm)
	{
		cm.setMsgID(chatStore_.size());
		chatStore_.add(cm);
	}

	static
	{
		// Need to initialize some until 
		chatStore_ = new ArrayList<ChatMessage>(Arrays.asList(new ChatMessage[]{
			new ChatMessage("#system#", "<luser> has entered"),
			new ChatMessage("luser", "Hey all!"),
			new ChatMessage("#system#", "<bozo> has entered"),
			new ChatMessage("luser", "Hey, bozo"),
			new ChatMessage("bozo", "Hey, l. Want a game?"),
			new ChatMessage("#system#", "<spongey> has entered"),
			new ChatMessage("#system#", "<spongey> has left"),
			new ChatMessage("#system#", "<bozo> has invited you to a game")}));
		for (int i = 0; i < chatStore_.size(); i++)
		{
			chatStore_.get(i).setMsgID(i);
		}
	}
}
