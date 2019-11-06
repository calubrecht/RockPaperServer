package online.cal.basePage.model;

import java.util.*;

import javax.annotation.*;

import org.springframework.stereotype.*;

import online.cal.basePage.model.ChatStore.*;

@Component("gameService")
public class GameService
{
	private List<GameListener> listeners_ = new ArrayList<GameListener>();
	
	private LinkedHashSet<String> gameSeekers_ = new LinkedHashSet<String>();
	
	@PostConstruct
	public void init()
	{
	  // Game Seekers queue
	  new Thread() {
		  public void run() {
		    while (true)
		    {
		    	Pair<String> match = matchPlayers();
		    	if (match == null)
		    	{
		    		try
					{
						Thread.sleep(1000);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	else
		    	{
		    		startGame(match);
		    	}
		    }
	  }}.start();
	}
	
	public synchronized void seekGame(String name)
	{
	  gameSeekers_.add(name);
	}
	
	public synchronized void endSeekGame(String name)
	{
	   gameSeekers_.remove(name);
	}
	
	public synchronized Pair<String> matchPlayers()
	{
		if (gameSeekers_.size() > 1)
		{
			Iterator<String> itr = gameSeekers_.iterator();
			String one = itr.next();
			itr.remove();
			String two = itr.next();
			itr.remove();
			return new Pair<String>(one, two);
		}
		return null;
	}
	
	public void startGame(Pair<String> match)
	{
		fireListeners(new GameMessage("gameID", "startGame", match.getFirst() + " v. " + match.getSecond(), match));
	}
	
	public void fireListeners(GameMessage msg)
	{
		listeners_.forEach(l -> l.onGameMessage(msg));
	}
	
	public void addListener(GameListener listener)
	{
		listeners_.add(listener);
	}
	public static interface GameListener
	{
		public void onGameMessage(GameMessage msg);
	}
}
