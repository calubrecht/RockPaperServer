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
	
	@Override
	public void onChat(ChatMessage cm)
	{
		template_.convertAndSend("/topic/chat", cm);

	}
	
	public void onGameMessage(GameMessage msg)
	{
		for (String p : msg.players_)
		{
		  for (String sessionID: sessionService_.getSessionIDs(p))
		  {
			  System.out.println("Send out game message to /queue/game-" +sessionID);
			  template_.convertAndSend("/queue/game-" +sessionID, msg);  
		  }
		}
	}

}
