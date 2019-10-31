package online.cal.basePage.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;
import online.cal.basePage.model.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class ChatController
{
	public static final String CHAT = "chat/";
	
	@RequestMapping(value=CHAT, method =RequestMethod.GET)
	public List<ChatMessage> getChats()
	{
		return ChatMessage.getChatMessages();
			
	}
	
}
