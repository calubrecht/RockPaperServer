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
	
	@Autowired
	WSSessionService sessionService_;
	
	
	HashMap<String, String> sessionMapping_ = new HashMap<String, String>();

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
		this.store_.addChat(new ChatMessage("#system#", "[" + p.getName() +"] joined"));
	}
	
	@SubscribeMapping("/user/queue/**")
	public void onSubscribeQ(Principal p, SimpMessageHeaderAccessor header)
	{
		sessionMapping_.put(p.getName(), header.getSessionId());
	    System.out.println(
	    		"Subscribe from user " + p.getName()  + "-" +  header.getNativeHeader("id") + "-" + header.getSessionId()
	    		+" on " +
	            header.getNativeHeader("destination") + "-" + header.getHeader("lookupDestination"));
	    
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
	     // System.out.println("Send out game message to " + p  + " on /queue/game");
		 // template_.convertAndSendToUser(p, "/queue/game", msg);
	    //  System.out.println("Send out game message to /queue/game-" + p + sessionMapping_.get(p));
	    //  template_.convertAndSend("/queue/game-" + p + sessionMapping_.get(p), msg);
		  for (String sessionID: sessionService_.getSessionIDs(p))
		  {
			  System.out.println("Send out game message to /queue/game-" +sessionID);
			  template_.convertAndSend("/queue/game-" +sessionID, msg);  
		  }
		}
	}

}
