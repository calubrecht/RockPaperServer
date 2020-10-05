package online.cal.basePage.controller;

import java.security.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;
import online.cal.basePage.model.*;
import online.cal.basePage.model.GameService.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class GameController
{
	@Autowired GameService gameService_;
	
	public static final String GAME= "game/";

	@RequestMapping(value=GAME + "seek", method =RequestMethod.POST)
	public GameMessage seekGame(Principal p )
	{
		gameService_.seekGame(p.getName());
		return new GameMessage("thisNewGame", "pending", "", new Pair<String>(p.getName(), p.getName()));
			
	}
	@RequestMapping(value=GAME + "endSeek", method =RequestMethod.POST)
	public GameMessage endSeekGame(Principal p )
	{
		gameService_.endSeekGame(p.getName());
		return new GameMessage("thisNewGame", "pending", "", new Pair<String>(p.getName(), p.getName()));
			
	}
	@RequestMapping(value=GAME + "startAIGame", method =RequestMethod.POST)
	public GameMessage startAIGAme(Principal p )
	{
		gameService_.startAIGame(p.getName());
		return new GameMessage("thisNewGame", "pending", "", new Pair<String>(p.getName(), p.getName()));
			
	}
	
	@RequestMapping(value=GAME + "resendActive", method =RequestMethod.POST)
	public void resendActive(Principal p )
	{
		gameService_.resendActive(p.getName());
			
	}
	
	@RequestMapping(value=GAME + "cancel", method =RequestMethod.POST)
	public GameMessage cancelGame(Principal p, @RequestBody GameMessage gm)
	{
		String id = gm.id_;
		try
		{
			gameService_.cancelGame(id);
		} catch (InvalidActionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new GameMessage("id", "canceled", "", new Pair<String>(p.getName(), p.getName()));
			
	}
	
	@RequestMapping(value=GAME + "invite", method=RequestMethod.POST)
	public GameMessage invite(Principal p, @RequestBody GameMessage gm)
	{
		String[] players = gm.getPlayers();
		if (players == null || !players[0].equals(p.getName()) || players[1].equals(p.getName()))
		{
			return new GameMessage("", "error", "Invalid players list", new Pair<String>(players[0], players[1]));
		}
		try
		{
			gameService_.inviteGame(p.getName(), players[1]);
		} catch (InvalidActionException e)
		{
			return new GameMessage("", "error", e.getMessage(), new Pair<String>(players[0], players[1]));
		}
		return new GameMessage("", "ok", "", new Pair<String>(players[0], players[1]));
	}
	
	@RequestMapping(value=GAME + "acceptInvite", method=RequestMethod.POST)
	public GameMessage acceptInvite(Principal p, @RequestBody GameMessage gm)
	{
		String id = gm.getGameID();
	    gameService_.acceptInvite(id);
		return new GameMessage("", "ok", "", null);
	}
}


