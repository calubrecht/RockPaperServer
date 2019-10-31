package online.cal.basePage.model;

import java.util.*;

public class ChatMessage
{
    private String userName_;
    private String chatText_;
    
    public ChatMessage()
    {
    	
    }
    
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
	
	/**
	 * Chat Store, replace with more permanent
	 */
	private static List<ChatMessage> chatStore_ = new ArrayList<ChatMessage>();
	public static List<ChatMessage> getChatMessages()
	{
		return Collections.unmodifiableList(chatStore_);
	}
	
	{
		// Need to initialize some until 
	}
}
