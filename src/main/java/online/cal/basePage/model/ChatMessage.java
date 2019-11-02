package online.cal.basePage.model;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class ChatMessage
{
    @JsonProperty("userName") private String userName_;
    @JsonProperty("chatText") private String chatText_;
    
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
	
	/**
	 * Chat Store, replace with more permanent
	 */
	private static List<ChatMessage> chatStore_ = new ArrayList<ChatMessage>();
	public static List<ChatMessage> getChatMessages()
	{
		return Collections.unmodifiableList(chatStore_);
	}
	
	public static void addChat(ChatMessage cm)
	{
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
	}
}
