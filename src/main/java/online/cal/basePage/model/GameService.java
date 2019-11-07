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
	
	Thread matchThread_;
	boolean running_ = true;
	
	@PostConstruct
	public void init()
	{
	  // Game Seekers queue
	  matchThread_ =  new Thread() {
		  public void run() {
		    while (running_)
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
	  }};
	  matchThread_.start();
	}
	
	@PreDestroy
	public void shutdown()
	{
		running_ = false;
		try
		{
			matchThread_.join();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
