package online.cal.basePage.model;

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import online.cal.basePage.util.*;

@Component("gameService")
public class GameService
{
    Logger logger = LoggerFactory.getLogger(getClass());
	private List<GameListener> listeners_ = new ArrayList<GameListener>();
	
	private LinkedHashSet<String> gameSeekers_ = new LinkedHashSet<String>();
	
	private Map<String, ActiveGame> activeGames_ = new HashMap<String, ActiveGame>();
	
	private Map<String, ActiveGame> playerGames_ = new HashMap<String, ActiveGame>();
	
	private Map<String, Pair<String>> invitations_ = new HashMap<String, Pair<String>>();
	
	private Bot defaultBot_;
	
	protected Notifier notifier_ = new Notifier();
	
	Thread matchThread_;
	int threadPollTime_ = 1000;
	boolean running_ = true;
	
	private final int GAME_LENGTH = 3;
	public final static Set<String> VALID_CHOICES = new HashSet<String>(Arrays.asList(new String[] {"rock", "paper", "scissors", "lizard", "spock"}));
	
	@Autowired
	BasePageUserService userService_;
	
	@PostConstruct
	public void init()
	{
	  defaultBot_ = new LastChoiceBot();
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
		    	    	notifier_.notifyWaiters();
						Thread.sleep(threadPollTime_);
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	else
		    	{
		    		startGameAndNotify	(match);
			    	notifier_.notifyWaiters();
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
	
	public void setBot(Bot defaultBot)
	{
		defaultBot_ = defaultBot;
	}
	
	
	public synchronized void seekGame(String name)
	{
	  gameSeekers_.add(name);
	}
	
	public synchronized void endSeekGame(String name)
	{
	   gameSeekers_.remove(name);
	}
	
	public synchronized void startAIGame(String name)
	{
		Pair<String> match = new Pair<String>(name, "AI - Player");
		ActiveGame game = new ActiveGame(makeID(), makeGameDesc(match.getFirst(), match.getSecond()), match);
		activeGames_.put(game.getID(), game);
		playerGames_.put(game.getPlayer(0), game);
		game.setBot(defaultBot_);
		game.start();
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
		String id = makeID();
        invitations_.put(id, new Pair<String>(inviter, invitee));
        sendInvitation(id, invitee, inviter);
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
		logger.info("Refuse inviation from {} to {}", inviter, invitee);
		fireListeners(new GameMessage("", "refuseInvite", invitee, new Pair<String>(inviter, null)));
	}
	
	
	public synchronized void sendInvitation(String id, String invitee, String inviter)
	{
		GameMessage gm = new GameMessage(id, "invite", makeGameDesc(inviter, invitee), new Pair<String>(invitee, inviter)); 
		gm.setDeliverTo(new String[] {invitee});
		fireListeners(gm);
	}
	
	public synchronized void acceptInvite(String id)
	{
		Pair<String> invitedGame = invitations_.get(id);
		GameMessage gm = new GameMessage(id, "acceptedInvite", makeGameDesc(invitedGame.getFirst(), invitedGame.getSecond()), new Pair<String>(invitedGame.getFirst(), invitedGame.getSecond())); 
		gm.setDeliverTo(new String[] {invitedGame.getFirst()});
		fireListeners(gm);
		invitations_.remove(id);
		if (invitedGame != null)
		{
			startGame(id, invitedGame);
		}
	}
	
	public void resendActive(String id)
	{
		ActiveGame game = playerGames_.get(id);
		if (game != null)
		{
			game.sendStatus(id);
		}
	}
	
	private void recordScore(String id, String winningPlayer, String losingPlayer) throws InvalidActionException
	{
		ActiveGame game = activeGames_.get(id);
		activeGames_.remove(id);
		playerGames_.remove(game.getPlayer(0));
		playerGames_.remove(game.getPlayer(1));
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
	
	private String makeGameDesc(String player1, String player2)
	{
		return player1 + " v. " + player2;
	}
	
	public void startGameAndNotify(Pair<String> match)
	{
		ActiveGame game = startGame(makeID(), match);
		game.start();
	}
	
	public ActiveGame startGame(String id, Pair<String> match)
	{
		ActiveGame game = new ActiveGame(id, makeGameDesc(match.getFirst(), match.getSecond()), match);
		activeGames_.put(game.getID(), game);
		playerGames_.put(game.getPlayer(0), game);
		playerGames_.put(game.getPlayer(1), game);	
		return game;
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
	
	protected List<ActiveGame> activeGames()
	{
		return Collections.unmodifiableList(new ArrayList<ActiveGame>(activeGames_.values()));
	}
	
	protected ActiveGame playGame(String name)
	{
		return playerGames_.get(name);
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
		String[] prevChoice = new String[2];
		List<List<String>> oldChoices_ = new ArrayList<List<String>>();
		Bot bot_;
		
		public ActiveGame(String ID, String description, Pair<String> match)
		{
			ID_ = ID;
			description_ = description;
			match_ = match;
			playerMap_.put(match.getFirst(),  0);
			playerMap_.put(match.getSecond(),  1);
			oldChoices_.add(new ArrayList<String>());
			oldChoices_.add(new ArrayList<String>());
		}
		
		public void setBot(Bot bot)
		{
			bot_ = bot;
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
			if (bot_ != null)
			{
				assert playerIndex == 0;
				String botChoice = bot_.nextChoice(oldChoices_.get(0), oldChoices_.get(1));
				oldChoices_.get(1).add(0, botChoice);
				lastChoice[1] = botChoice;
			}
			oldChoices_.get(playerIndex).add(0, choice);
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
				prevChoice = lastChoice.clone();
				gm.setChoices(prevChoice);
				lastChoice[0] = lastChoice[1] = null;
				fireListeners(gm);
				return;
			}
			if (!sendResult(lastChoice[0], lastChoice[1], match_.getFirst(), match_.getSecond()))
			{
				sendResult(lastChoice[1], lastChoice[0], match_.getSecond(), match_.getFirst());
			}
		}
		
		private String[] getResult(String choice1, String choice2, String player1, String player2)
		{
			if (choice1.equals(choice2))
			{
				return new String[] {null, "Tie!"};
			}
			if (choice1.equals("rock") && choice2.equals("scissors"))
            {
				return new String[] {player1, "Rock smashes scissors."};
            }
			if (choice1.equals("rock") && choice2.equals("lizard"))
            {
				return new String[] {player1,  "Rock crushes lizard."};
            }		
			if (choice1.equals("scissors") && choice2.equals("paper"))
            {
	            return new String[] {player1, "Scissors cut paper."};
            }
			if (choice1.equals("scissors") && choice2.equals("lizard"))
            {
	            return new String[] {player1, "Scissors decapitate lizard."};
            }
			if (choice1.equals("paper") && choice2.equals("rock"))
            {
	            return new String[] {player1, "Paper covers rock."};
            }

			if (choice1.equals("paper") && choice2.equals("spock"))
          {
	            return new String[] {player1, "Paper disproves Spock."};
          }
			if (choice1.equals("lizard") && choice2.equals("spock"))
	          {
		            return new String[] {player1, "Lizard poisons Spock."};
	          }
			if (choice1.equals("lizard") && choice2.equals("paper"))
	          {
		            return new String[] {player1, "Lizard eats paper."};
	          }
			if (choice1.equals("spock") && choice2.equals("rock"))
	          {
		            return new String[] {player1, "Spock vaporizes rock."};
	          }
			if (choice1.equals("spock") && choice2.equals("scissors"))
	          {
		            return new String[] {player1, "Spock breaks scissors."};
	          }
			return new String[] {};
		}
		
		private boolean sendResult(String choice1, String choice2, String player1, String player2)
		{
			String[] res = getResult(choice1, choice2, player1, player2);
			if (res.length == 0)
			{
				return false;
			}
			if (res[0] == null)
			{
			  return false;
			}
			sendWin(res[0], res[1]);
			return true;
		}
		
		private void sendWin(String winner, String description)
		{
			round++;
			scores_[playerMap_.get(winner)]++;
			GameMessage gm = new GameMessage(ID_, "point", description, match_);
			prevChoice = lastChoice.clone();
			gm.setChoices(prevChoice);
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
		
		public synchronized void sendStatus(String player)
		{
			GameMessage msg = new GameMessage(ID_, "resendGame", description_, match_);
			msg.setDeliverTo(new String[]{ player });
			msg.setScores(scores_);
			msg.setRound(round);
			String[] choices = new String[2];
			int playerIdx = playerMap_.get(player);
			int oppId = playerIdx == 0 ? 1 : 0;
			if (lastChoice[playerIdx] == null)
			{
				choices = prevChoice.clone();
			}
			else
			{
				choices[playerIdx] = lastChoice[playerIdx];
			}
			msg.setChoices(choices);
			if (choices[0] != null && choices[1] != null)
			{
			   String[] res = getResult(choices[0], choices[1], match_.getFirst(), match_.getSecond());
			   if (res.length == 0)
			   {
				   res = getResult(choices[1], choices[0], match_.getSecond(), match_.getSecond());
			   }
			   msg.setWinner(res[0]);
			   msg.setDetail2(res[1]);
			}
			// Check for a result
			fireListeners(msg);
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
	
	public interface Bot
	{
		String nextChoice(List<String> opponentsChoices, List<String>previousChoices);
	}
	
	public static class LastChoiceBot extends FullRandomBot
	{	
		public LastChoiceBot(long seed)
		{
			super(seed);
		}
		
		public LastChoiceBot()
		{
			super();
		}
		
		public String nextChoice(List<String> opponentsChoices, List<String>previousChoices)
		{
			if (opponentsChoices.size() == 0)
			{
				return super.nextChoice(opponentsChoices, previousChoices);
			}
			return opponentsChoices.get(0);
		}
	}
	
	public static class FullRandomBot implements Bot
	{
		Random r_;
		List<String> CHOICES = new ArrayList<String>(VALID_CHOICES);
		
		public FullRandomBot(long seed)
		{
			r_ = new Random(seed);
		}
		
		public FullRandomBot()
		{
			this(System.currentTimeMillis());
		}
		
		public String nextChoice(List<String> opponentsChoices, List<String>previousChoices)
		{
			return CHOICES.get(Math.abs(r_.nextInt()) % CHOICES.size());
		}
	}
}
