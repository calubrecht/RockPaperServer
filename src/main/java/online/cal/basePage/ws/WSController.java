package online.cal.basePage.ws;

import java.security.*;
import java.util.*;

import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.annotation.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import online.cal.basePage.model.*;
import online.cal.basePage.model.ChatStore.*;
import online.cal.basePage.model.GameService.*;

@Controller
public class WSController implements ChatListener, GameListener, BasePageUserService.UserListener
{
    Logger logger = LoggerFactory.getLogger(getClass());
	private SimpMessagingTemplate template_;

	@Autowired
	ChatStore store_;
	@Autowired
	GameService gameService_;
	@Autowired
	BasePageUserService userService_;

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
		this.userService_.addListener(this);
	}

	@MessageMapping("/send/gameMessage")
	public void onReceivedGameMessage(Principal p, MessageHeaderAccessor header, GameMessage message)
	{
		String action = message.getAction();
		try
		{
			if (action.equals("makeChoice"))
			{	
				gameService_.makeChoice(message.getGameID(), p.getName(), message.getDetail());
			}
		} catch (InvalidActionException iae)
		{
			onGameMessage(new GameMessage(message.getGameID(), "ERROR", iae.getMessage(),
					new Pair<String>(p.getName(), null)));
		}
	}

	public void onChat(ChatMessage cm)
	{
		template_.convertAndSend("/topic/chat", cm);

	}
	
	public void onUser(BasePageUser bpu)
	{
		template_.convertAndSend("/topic/users", bpu);
	}
	
	@SubscribeMapping("/topic/users")
	public BasePageUser onUserSubscribe(@Headers Map<String, Object> headers) {
		 UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)headers.get("simpUser");
		 if (token == null)
		 {
			 return null;
		 }
		 logger.info("onUserSubscribe " + token.getPrincipal());
	     return userService_.getUser(token.getPrincipal().toString());
	}

	public void onGameMessage(GameMessage msg)
	{
		for (String p : msg.getDeliverTo())
		{
			if (p == null)
			{
				continue;
			}
			for (String sessionID : sessionService_.getSessionIDs(p))
			{
				logger.info("Send out game message to /queue/game-{}", sessionID);
				template_.convertAndSend("/queue/game-" + sessionID, msg);
			}
		}
	}

}
