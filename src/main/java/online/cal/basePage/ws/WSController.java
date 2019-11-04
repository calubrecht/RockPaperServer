package online.cal.basePage.ws;

import java.text.*;
import java.util.*;

import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.*;
import org.springframework.stereotype.*;

import online.cal.basePage.model.*;
import online.cal.basePage.model.ChatStore.*;

@Controller
public class WSController implements ChatListener
{
	private SimpMessagingTemplate template_;

	@Autowired
	ChatStore store_;

	@Autowired
	WSController(SimpMessagingTemplate template)
	{
		template_ = template;
	}

	@PostConstruct
	public void init()
	{
		this.store_.addListener(this);
	}

	@MessageMapping("/send/message")
	public void onReceivedMessage(String message)
	{
		//template_.convertAndSend("/topic/chat", new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - " + message);
	}

	@Override
	public void onChat(ChatMessage cm)
	{
		template_.convertAndSend("/topic/chat", cm);

	}

	// public void send(String message)

}
