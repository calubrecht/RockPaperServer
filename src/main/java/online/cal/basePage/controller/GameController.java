package online.cal.basePage.controller;

import java.security.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;
import online.cal.basePage.model.*;

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
}


