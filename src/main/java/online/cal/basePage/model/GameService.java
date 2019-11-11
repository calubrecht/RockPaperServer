package online.cal.basePage.model;

import java.security.*;
import java.util.*;

import javax.annotation.*;

import org.springframework.stereotype.*;

import online.cal.basePage.model.ChatStore.*;

@Component("gameService")
public class GameService
{
	private List<GameListener> listeners_ = new ArrayList<GameListener>();
	
	private LinkedHashSet<String> gameSeekers_ = new LinkedHashSet<String>();
	
	private Map<String, ActiveGame> activeGames_ = new HashMap<String, ActiveGame>();
	
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
	
	private String makeID()
	{
	    Random random = new Random();
	    byte[] bytes = new byte[21];
	    random.nextBytes(bytes);
	    String b64 = Base64.getEncoder().encodeToString(bytes);
		return "game_" + b64;
	}
	
	public void startGame(Pair<String> match)
	{
		ActiveGame game = new ActiveGame(makeID(), match.getFirst() + " v. " + match.getSecond(), match);
		game.start();
		activeGames_.put(game.getID(), game);
	}
	
	public void makeChoice(String gameID, String player, String choice) throws InvalidActionException
	{
		ActiveGame game = activeGames_.get(gameID);
		if (game == null)
		{
			throw new InvalidActionException("Unrecognized game ID");
		}
		game.makeChoice(player,  choice);
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
	
	class ActiveGame
	{
		private String ID_;
		private String description_;
		private Pair<String> match_;
		private Map<String, Integer> playerMap_ = new HashMap<String, Integer>();
		int round = 0;
		int[] scores_ = new int[2];
		
		int[] points_ = new int[2];
		String[] lastChoice = new String[2];
		
		public final Set<String> VALID_CHOICES = new HashSet<String>(Arrays.asList(new String[] {"rock", "paper", "scissors", "lizard", "spock"}));
		
		public ActiveGame(String ID, String description, Pair<String> match)
		{
			ID_ = ID;
			description_ = description;
			match_ = match;
			playerMap_.put(match.getFirst(),  0);
			playerMap_.put(match.getSecond(),  1);
		}
		
		public void start()
		{
			fireListeners(new GameMessage(ID_,  "startGame", description_, match_));
		}
		
		public void makeChoice(String player, String choice) throws InvalidActionException
		{
			validatePlayer(player);
			validateChoice(choice);
			int playerIndex = playerMap_.get(player);
			if (lastChoice[playerIndex] != null)
			{
				throw new InvalidActionException(player + " has already made a selection.");
			}
			lastChoice[playerIndex] = choice;
			tryResolve();
		}
		
		private synchronized void tryResolve()
		{
			if (lastChoice[0] == null || lastChoice[1] == null)
			{
				return;
			}
			if (lastChoice[0].equals(lastChoice[1]))
			{
				// Tie. Try again			
				GameMessage gm = new GameMessage(ID_,  "TIE", "Tie!", match_);
				gm.setChoices(lastChoice.clone());
				lastChoice[0] = lastChoice[1] = null;
				fireListeners(gm);
				return;
			}
			if (!sendResult(lastChoice[0], lastChoice[1], match_.getFirst(), match_.getSecond()))
			{
				sendResult(lastChoice[1], lastChoice[0], match_.getSecond(), match_.getFirst());
			}
		}
		
		private boolean sendResult(String choice1, String choice2, String player1, String player2)
		{
			if (choice1.equals("rock") && choice2.equals("scissors"))
            {
	            sendWin(player1, "Rock smashes scissors.");
	            return true;
            }
			if (choice1.equals("rock") && choice2.equals("lizard"))
            {
	            sendWin(player1, "Rock crushes lizard.");
	            return true;
            }		
			if (choice1.equals("scissors") && choice2.equals("paper"))
            {
	            sendWin(player1, "Scissors cut paper.");
	            return true;
            }
			if (choice1.equals("scissors") && choice2.equals("lizard"))
            {
	            sendWin(player1, "Scissors decapitate lizard.");
	            return true;
            }
			if (choice1.equals("paper") && choice2.equals("rock"))
            {
	            sendWin(player1, "Paper covers rock.");
	            return true;
            }

			if (choice1.equals("paper") && choice2.equals("spock"))
          {
	            sendWin(player1, "Paper disproves Spock.");
	            return true;
          }
			if (choice1.equals("lizard") && choice2.equals("spock"))
	          {
		            sendWin(player1, "Lizard poisons Spock.");
		            return true;
	          }
			if (choice1.equals("lizard") && choice2.equals("paper"))
	          {
		            sendWin(player1, "Lizard eats paper.");
		            return true;
	          }
			if (choice1.equals("spock") && choice2.equals("rock"))
	          {
		            sendWin(player1, "Spock vaporizes rock.");
		            return true;
	          }
			if (choice1.equals("spock") && choice2.equals("scissors"))
	          {
		            sendWin(player1, "Spock breaks scissors.");
		            return true;
	          }
			return false;
		}
		
		private void sendWin(String winner, String description)
		{
			round++;
            scores_[playerMap_.get(winner)]++;
			GameMessage gm = new GameMessage(ID_,  "point", description, match_);
			gm.setChoices(lastChoice.clone());
			gm.setWinner(winner);
			gm.setRound(round);
			gm.setScores(scores_);
			lastChoice[0] = lastChoice[1] = null;
			fireListeners(gm);
		}
		
		public String getID()
		{
			return ID_;
		}
		
		private void validatePlayer(String player) throws InvalidActionException
		{
			if (!playerMap_.containsKey(player))
			{
				throw new InvalidActionException(player + " is not involved in this game.");
			}
		}
		
		private void validateChoice(String choice) throws InvalidActionException
		{
			if (!VALID_CHOICES.contains(choice))
			{
				throw new InvalidActionException(choice + " is not a valid choice.");
			}
		}
		
		
	}
	

	public static class InvalidActionException extends Exception
	{
		public InvalidActionException(String msg)
		{
			super(msg);
		}
	}
}
