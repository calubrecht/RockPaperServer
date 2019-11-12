package online.cal.basePage.model;

import com.fasterxml.jackson.annotation.*;

public class ChatMessage
{
    @JsonProperty("userName") private String userName_;
    @JsonProperty("chatText") private String chatText_;
    @JsonProperty("msgID") private long msgID_;
    
    public ChatMessage() {}
    
    public ChatMessage(String userName, String chatText)
    {
    	userName_ = userName;
    	chatText_ = chatText;
    }
    
    
    public ChatMessage(String userName, String chatText, long msgID)
    {
    	userName_ = userName;
    	chatText_ = chatText;
    	msgID_ = msgID;
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
}
