package online.cal.basePage.ws;

import java.security.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;

import online.cal.basePage.model.*;
import online.cal.basePage.model.ChatStore.*;
import online.cal.basePage.model.GameService.*;

@Controller
public class WSController implements ChatListener, GameListener
{
	private SimpMessagingTemplate template_;

	@Autowired
	ChatStore store_;
	@Autowired
	GameService gameService_;
	
	
	HashMap<String, Principal> userMapping_ = new HashMap<String, Principal>();

	@Autowired
	WSController(SimpMessagingTemplate template)
	{
		template_ = template;
	}

	@PostConstruct
	public void init()
	{
		this.store_.addListener(this);
		this.gameService_.addListener(this);
	}
	
	@SubscribeMapping("/topic/chat")
	public void onSubscribe(Principal p)
	{
		userMapping_.put(p.getName(), p);
		this.store_.addChat(new ChatMessage("#system#", "[" + p.getName() +"] joined"));
	}
	
	@SubscribeMapping("/queue/**")
	public void onSubscribeQ(Principal p)
	{
		userMapping_.put(p.getName(), p);
		//this.store_.addChat(new ChatMessage("#system#", "[" + p.getName() +"] joined"));
	}

	@MessageMapping("/send/message")
	public void onReceivedMessage(Principal p, TextMessage message)
	{
		this.store_.addChat(new ChatMessage("#system#", "[" + p.getName() +"] says '" + message.getPayload() + "'"));	
	}

	@Override
	public void onChat(ChatMessage cm)
	{
		template_.convertAndSend("/topic/chat", cm);

	}
	
	public void onGameMessage(GameMessage msg)
	{
		for (String p : msg.players_)
		{
		  template_.convertAndSendToUser(p, "/queue/game", msg);
		}
	}

}
