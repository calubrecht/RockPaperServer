package online.cal.basePage.controller;

import java.util.*;

import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;
import online.cal.basePage.model.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class ChatController
{
	public static final String CHAT = "chat/";
	
	@RequestMapping(value=CHAT + "chats", method =RequestMethod.GET)
	public List<ChatMessage> getChats()
	{
		return ChatStore.getInstance().getChatMessages();
			
	}
	
	@RequestMapping(value=CHAT + "chatsSince/{id}", method =RequestMethod.GET)
	public PollResult<List<ChatMessage>> getChatsSince(@PathVariable Long id)
	{
		List<ChatMessage> chats = ChatStore.getInstance().getChatMessagesSince(id);
		if (chats.isEmpty())
		{
			return new PollResult<List<ChatMessage>>(id, chats);
		}
		return new PollResult<List<ChatMessage>>(chats.get(chats.size()-1).getMsgID(), chats);
			
	}
	
	@RequestMapping(value=CHAT + "chat", method =RequestMethod.POST)
	public ChatMessage addChat(@RequestBody ChatMessage cm)
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userName = auth.getName();
		ChatMessage postMessage = new ChatMessage(userName, cm.getChatText());
		ChatStore.getInstance().addChat(postMessage);
		return postMessage;
			
	}
	
}
