package online.cal.basePage.model;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

public class ChatMessage
{
	public static final String SYSTEM = "#system#";
	
    @JsonProperty("userName") private String userName_;
    @JsonProperty("chatText") private String chatText_;
    @JsonProperty("msgID") private long msgID_;
    
    
    @JsonIgnore protected Date dt_;
    
    public ChatMessage() {}
    
    public ChatMessage(String userName, String chatText)
    {
    	userName_ = userName;
    	chatText_ = chatText;
    	dt_ = new Date();
    }
    
    
    public ChatMessage(String userName, String chatText, long msgID)
    {
    	userName_ = userName;
    	chatText_ = chatText;
    	msgID_ = msgID;
    	dt_ = new Date();
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
	
	public boolean shouldExpire()
	{
		return getUserName().equals(SYSTEM) && dt_.toInstant().plusSeconds(60 * 60).isBefore(new Date().toInstant());
	}
}
