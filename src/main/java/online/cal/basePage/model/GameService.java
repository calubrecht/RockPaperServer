package online.cal.basePage.model;

import java.util.*;

import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component("gameService")
public class GameService
{
	private List<GameListener> listeners_ = new ArrayList<GameListener>();
	
	private LinkedHashSet<String> gameSeekers_ = new LinkedHashSet<String>();
	
	private Map<String, ActiveGame> activeGames_ = new HashMap<String, ActiveGame>();
	
	private Map<String, ActiveGame> playerGames_ = new HashMap<String, ActiveGame>();
	
	private Map<String, Pair<String>> invitations_ = new HashMap<String, Pair<String>>();
	
	Thread matchThread_;
	boolean running_ = true;
	
	private final int GAME_LENGTH = 3;
	
	@Autowired
	BasePageUserService userService_;
	
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
	
	public synchronized void inviteGame(String inviter, String invitee) throws InvalidActionException
	{
		if (gameSeekers_.contains(inviter))
		{
			gameSeekers_.remove(inviter);
		}			
		if (playerGames_.containsKey(inviter))
		{
			throw new InvalidActionException("Cannot invite a player while in a game.");
		}
		if (playerGames_.containsKey(invitee))
		{
			refuseInvitation(inviter, invitee);
			return;
		}
        invitations_.put(invitee, new Pair<String>(inviter, invitee));
        sendInvitation(invitee, inviter);
	}
	
	public synchronized void cancelGame(String id) throws InvalidActionException
	{
		ActiveGame game = activeGames_.get(id);
		if (game == null)
		{
			throw new InvalidActionException("Unrecognized game ID");
		}
		game.cancel();
		activeGames_.remove(id);
		playerGames_.remove(game.getPlayer(0));
		playerGames_.remove(game.getPlayer(1));
	}
	
	public synchronized void refuseInvitation(String inviter, String invitee)
	{
		invitations_.remove(invitee);
		fireListeners(new GameMessage("", "refuseInvite", "", new Pair<String>(inviter, inviter)));
	}
	
	
	public synchronized void sendInvitation(String invitee, String inviter)
	{
		fireListeners(new GameMessage("", "invite", "", new Pair<String>(inviter, inviter)));
	}
	
	public synchronized void acceptInvite(String invitee)
	{
		Pair<String> invitedGame = invitations_.get(invitee);
		invitations_.remove(invitee);
		if (invitedGame != null)
		{
			startGame(invitedGame);
		}
	}
	
	public void recordScore(String id, String winningPlayer, String losingPlayer) throws InvalidActionException
	{
		ActiveGame game = activeGames_.get(id);
		if (game == null)
		{
			throw new InvalidActionException("Unrecognized game ID");
		}
		activeGames_.remove(id);
		playerGames_.remove(game.getPlayer(0));
		playerGames_.remove(game.getPlayer(1));
		if (winningPlayer.contentEquals(losingPlayer))
		{
			// Don't record scores for players playing themselves.
			return;
		}
		userService_.recordWin(winningPlayer);
		userService_.recordLoss(losingPlayer);
		
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
		playerGames_.put(game.getPlayer(0), game);
		playerGames_.put(game.getPlayer(1), game);	
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
		private boolean active_ = true;
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
		
		public synchronized void cancel()
		{
			active_ = false;
			fireListeners(new GameMessage(ID_,  "Canceled", description_, match_));
		}
		
		public synchronized void makeChoice(String player, String choice) throws InvalidActionException
		{
			if (!active_)
			{
				throw new InvalidActionException("Game is no longer active");	
			}
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
			GameMessage gm = new GameMessage(ID_, "point", description, match_);
			gm.setChoices(lastChoice.clone());
			gm.setWinner(winner);
			gm.setRound(round);
			gm.setScores(scores_);
			lastChoice[0] = lastChoice[1] = null;
			fireListeners(gm);

			if (scores_[0] >= GAME_LENGTH || scores_[1] >= GAME_LENGTH)
			{
				active_ = false;
				int winnerIdx = playerMap_.get(winner);
				int loserIdx = winnerIdx == 0 ? 1 : 0;
				GameMessage gameEnd = new GameMessage(ID_, "Finished",
						winner + " wins. " + scores_[winnerIdx] + " v " + scores_[loserIdx], match_);
				fireListeners(gameEnd);
				try
				{
					GameService.this.recordScore(ID_, winner, loserIdx == 0 ? match_.getFirst() : match_.getSecond());
				} catch (InvalidActionException e)
				{
					// Shoudln't get here.
				}
			}
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
		
		public String getPlayer(int index)
		{
		   return index == 0 ? match_.getFirst() : match_.getSecond();
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
