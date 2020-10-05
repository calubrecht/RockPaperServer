package online.cal.basePage.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.*;

import online.cal.basePage.model.GameService.*;

public class GameServiceTest implements GameListener
{

	GameService service_;
	MockUserService userService_;
	GameMessage lastMessage_;
	List<GameMessage> allMessages_;
	int pollTime = 60;
	
	
	private void waitForPoll() throws InterruptedException
	{
		  service_.notifier_.waitForNotify();
	}
	
	@Before
	public void setUp()
	{
		BasePageUserService.INSTANCE = null;
		service_ = new GameService();
		userService_ = new MockUserService();
		service_.userService_ = userService_;
		service_.addListener(this);
		service_.threadPollTime_ = pollTime;
		service_.init();
		allMessages_ = new ArrayList<GameMessage>();
	}
	
	@After
	public void tearDown()
	{
		service_.shutdown();
		service_ = null;
		userService_ = null;
		allMessages_ = null;
		lastMessage_ = null;
	}
	
	@Test
	public void testMatch() throws InterruptedException
	{
        service_.seekGame("Bobbo");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 1);
        assertTrue(service_.playGame("Bobbo") != null);
        assertTrue(service_.playGame("Frank") != null);
        
        ActiveGame ag = service_.activeGames().get(0);
        ActiveGame agBobbo = service_.playGame("Bobbo");
        ActiveGame agFrank = service_.playGame("Frank");
        
        assertTrue(ag == agBobbo);
        assertTrue(ag == agFrank);
        assertEquals(ag.getPlayer(0), "Bobbo");
        assertEquals(ag.getPlayer(1), "Frank");
	}
	
	@Test
	public void testCancelSeek() throws InterruptedException
	{
        service_.seekGame("Bobbo");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 0);
        service_.endSeekGame("Bobbo");
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 0);
	}
	
	@Test
	public void testGame() throws InterruptedException, InvalidActionException
	{
        service_.seekGame("Bobbo");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 1);
        assertTrue(service_.playGame("Bobbo") != null);
        assertTrue(service_.playGame("Frank") != null);
        ActiveGame ag = service_.activeGames().get(0);
        
        ag.makeChoice("Bobbo", "rock");
        ag.makeChoice("Frank", "spock");
        assertEquals(ag.scores_[0], 0);
        assertEquals(ag.scores_[1], 1);
        
        ag.makeChoice("Bobbo", "spock");
        ag.makeChoice("Frank", "paper");
        assertEquals(ag.scores_[0], 0);
        assertEquals(ag.scores_[1], 2);
        
        assertEquals(userService_.getWins("Bobbo"), 0);
        assertEquals(userService_.getWins("Frank"), 0);
        assertEquals(userService_.getLosses("Bobbo"), 0);
        assertEquals(userService_.getLosses("Frank"), 0);
        
        ag.makeChoice("Bobbo", "rock");
        ag.makeChoice("Frank", "paper");
        assertEquals(userService_.getWins("Bobbo"), 0);
        assertEquals(userService_.getWins("Frank"), 1);
        assertEquals(userService_.getLosses("Bobbo"), 1);
        assertEquals(userService_.getLosses("Frank"), 0);
        assertEquals(lastMessage_.getDetail(), "Frank wins. 3 v 0");
	}
	
	@Test
	public void testBadAI() throws Exception
	{
		service_.setBot(new GameService.LastChoiceBot(1));
		service_.startAIGame("Bobbo");
		// LastChoiceBot chooses last choice of other player. First round is random. Random seed 1->First Choice = rock
		assertEquals(service_.activeGames().size(), 1);
		ActiveGame ag = service_.activeGames().get(0);
		String id = ag.getID();
		service_.makeChoice(id, "Bobbo", "paper");
	    assertEquals(lastMessage_.choices_[1], "rock");
	    assertEquals(ag.scores_[0], 1);
	    assertEquals(ag.scores_[1], 0);
	    
	    service_.makeChoice(id, "Bobbo", "scissors");
	    assertEquals(lastMessage_.choices_[1], "paper");
	    assertEquals(ag.scores_[0], 2);
	    assertEquals(ag.scores_[1], 0);
	    
	    service_.makeChoice(id, "Bobbo", "lizard");
	    assertEquals(lastMessage_.choices_[1], "scissors");
	    assertEquals(ag.scores_[0], 2);
	    assertEquals(ag.scores_[1], 1);
	    
	    service_.makeChoice(id, "Bobbo", "rock");
	    assertEquals(lastMessage_.getDetail(), "Bobbo wins. 3 v 1");
	    assertEquals(allMessages_.get(1).choices_[1], "lizard");
	    assertEquals(ag.scores_[0], 3);
	    assertEquals(ag.scores_[1], 1);
	    assertEquals(userService_.getWins("Bobbo"), 1);
	    assertEquals(userService_.getWins("AI - Player"), 0);
	    assertEquals(userService_.getLosses("Bobbo"), 0);
	    assertEquals(userService_.getLosses("AI - Player"), 1);
	}

	@Test
	public void testRefuseInvitation() throws Exception
	{
		allMessages_.clear();
		service_.refuseInvitation("JoeBob", "Michael");

		assertEquals(1, allMessages_.size());
		assertEquals("Michael", lastMessage_.getDetail());
		assertEquals("refuseInvite", lastMessage_.getAction());
		assertEquals("JoeBob", lastMessage_.getPlayers()[0]);
		assertEquals(null, lastMessage_.getPlayers()[1]);
	}
	
	@Test
	public void testInviteWhileActive() throws InterruptedException
	{
        service_.seekGame("Bobbo");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 1);
        
        try
		{
			service_.inviteGame("Bobbo", "Pete");
			assertTrue("Expected invite to throw if user already in game", false);
		} catch (InvalidActionException e)
		{
		} 
	}
	
	@Test
	public void testInviteWhileSeeking() throws InterruptedException, InvalidActionException
	{
		service_.seekGame("Bobbo");
		service_.inviteGame("Bobbo", "Pete");
		
        service_.seekGame("Frank");
        waitForPoll();
        // Bobbo left the seek queue, so Frank's game doesn't start
        assertEquals(service_.activeGames().size(), 0);
	}
	
	@Test
	public void testInvitePlayerInGame() throws InterruptedException, InvalidActionException
	{
        service_.seekGame("Bobbo");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 1);
        
        clearMessages();
		service_.inviteGame("Pete", "Bobbo");
		assertEquals("refuseInvite", lastMessage_.getAction());
        
	}
	
	@Test
	public void testAcceptInvite() throws InvalidActionException
	{
		service_.inviteGame("Bobbo", "Pete");
		assertEquals("invite", lastMessage_.getAction());
		assertEquals("Pete", lastMessage_.getDeliverTo()[0]);
		assertEquals(1, lastMessage_.getDeliverTo().length);
		assertEquals("Pete", lastMessage_.getPlayers()[0]);
		assertEquals("Bobbo", lastMessage_.getPlayers()[1]);
		String gameID = lastMessage_.id_;
		
		clearMessages();
		service_.acceptInvite(gameID);
		assertEquals(1, allMessages_.size());
		assertEquals("acceptedInvite", allMessages_.get(0).action_);
		assertEquals("Bobbo", lastMessage_.getDeliverTo()[0]);
		assertEquals(1, lastMessage_.getDeliverTo().length);
		
		ActiveGame gamePete = service_.playGame("Pete");
		ActiveGame gameBobbo = service_.playGame("Bobbo");
		assertEquals(gameID, gamePete.getID());
		assertEquals(gamePete, gameBobbo);
		
	}
	
	@Test
	public void testCancelGame() throws InterruptedException, InvalidActionException
	{
        service_.seekGame("Bobbo");
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 1);
        assertTrue(service_.playGame("Bobbo") != null);
        assertTrue(service_.playGame("Frank") != null);
        ActiveGame ag = service_.activeGames().get(0);
        
        clearMessages();
        service_.cancelGame(ag.getID());
        assertEquals(1, allMessages_.size());
        assertEquals("Canceled", lastMessage_.getAction());
        assertEquals(2, lastMessage_.getDeliverTo().length);
        assertEquals("Bobbo", lastMessage_.getDeliverTo()[0]);
        assertEquals("Frank", lastMessage_.getDeliverTo()[1]);
        assertEquals(null, service_.playGame("Bobbo"));
        assertEquals(null, service_.playGame("Frank"));
	}
	
	@Test
	public void testCancelNonexistantGame()
	{
		try
		{
			service_.cancelGame("BOOGEDITY");
			assertTrue("Canceling nonexistant game should throw", false);
		} catch (InvalidActionException e)
		{
		}
	}
	
	@Test
	public void testResendActive() throws InterruptedException, InvalidActionException
	{
        service_.seekGame("Bobbo");
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        assertEquals(service_.activeGames().size(), 1);
        assertTrue(service_.playGame("Bobbo") != null);
        assertTrue(service_.playGame("Frank") != null);
        ActiveGame ag = service_.activeGames().get(0);
        String id = ag.getID();
		service_.makeChoice(id, "Bobbo", "scissors");
		service_.makeChoice(id, "Frank", "rock");
		
		clearMessages();
		service_.resendActive("Frank");
		assertEquals(1, allMessages_.size());
		assertEquals("resendGame", lastMessage_.getAction());
		assertEquals(id, lastMessage_.id_);
		assertEquals(1, lastMessage_.getDeliverTo().length);
		assertEquals("Frank", lastMessage_.getDeliverTo()[0]);
		
		assertEquals("Bobbo", lastMessage_.getPlayers()[0]);
		assertEquals("Frank", lastMessage_.getPlayers()[1]);
		assertEquals(0, lastMessage_.getScores()[0]);
		assertEquals(1, lastMessage_.getScores()[1]);
		assertEquals("Bobbo v. Frank", lastMessage_.getDetail());
		assertEquals("Rock smashes scissors.", lastMessage_.getDetail2());
		assertEquals(1, lastMessage_.getRound());
		assertEquals("scissors", lastMessage_.choices_[0]);
		assertEquals("rock", lastMessage_.choices_[1]);
		
		service_.makeChoice(id, "Bobbo", "scissors");
		clearMessages();
		service_.resendActive("Frank");
		
		assertEquals("Bobbo", lastMessage_.getPlayers()[0]);
		assertEquals("Frank", lastMessage_.getPlayers()[1]);
		assertEquals(0, lastMessage_.getScores()[0]);
		assertEquals(1, lastMessage_.getScores()[1]);
		assertEquals("Bobbo v. Frank", lastMessage_.getDetail());
		assertEquals("Rock smashes scissors.", lastMessage_.getDetail2());
		assertEquals(1, lastMessage_.getRound());
		assertEquals("scissors", lastMessage_.choices_[0]);
		assertEquals("rock", lastMessage_.choices_[1]);
		
		clearMessages();
		service_.resendActive("Bobbo");
		
		assertEquals("Bobbo", lastMessage_.getDeliverTo()[0]);
		assertEquals("Bobbo", lastMessage_.getPlayers()[0]);
		assertEquals("Frank", lastMessage_.getPlayers()[1]);
		assertEquals(0, lastMessage_.getScores()[0]);
		assertEquals(1, lastMessage_.getScores()[1]);
		assertEquals("Bobbo v. Frank", lastMessage_.getDetail());
		assertEquals(null, lastMessage_.getDetail2());
		assertEquals(1, lastMessage_.getRound());
		assertEquals("scissors", lastMessage_.choices_[0]);
		assertEquals(null, lastMessage_.choices_[1]);
		
		service_.makeChoice(id, "Frank", "scissors");
		clearMessages();
		service_.resendActive("Frank");
		assertEquals("Bobbo", lastMessage_.getPlayers()[0]);
		assertEquals("Frank", lastMessage_.getPlayers()[1]);
		assertEquals(0, lastMessage_.getScores()[0]);
		assertEquals(1, lastMessage_.getScores()[1]);
		assertEquals("Bobbo v. Frank", lastMessage_.getDetail());
		assertEquals("Tie!", lastMessage_.getDetail2());
		assertEquals(1, lastMessage_.getRound());
		assertEquals("scissors", lastMessage_.choices_[0]);
		assertEquals("scissors", lastMessage_.choices_[1]);
		
		service_.makeChoice(id, "Frank", "spock");
		clearMessages();
		service_.resendActive("Frank");
		assertEquals("Bobbo", lastMessage_.getPlayers()[0]);
		assertEquals("Frank", lastMessage_.getPlayers()[1]);
		assertEquals(0, lastMessage_.getScores()[0]);
		assertEquals(1, lastMessage_.getScores()[1]);
		assertEquals("Bobbo v. Frank", lastMessage_.getDetail());
		assertEquals(null, lastMessage_.getDetail2());
		assertEquals(1, lastMessage_.getRound());
		assertEquals(null, lastMessage_.choices_[0]);
		assertEquals("spock", lastMessage_.choices_[1]);
	}
	
	@Test
	public void testAllCases() throws InterruptedException
	{
        service_.seekGame("Bobbo");
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        ActiveGame ag = service_.activeGames().get(0);
        
        String[] res;
        res = ag.getResult("Bran", "Oats", "Bobbo", "Frank");
        assertEquals(0, res.length);
        
        
        // Frontways
        res = ag.getResult("scissors", "spock", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Spock breaks scissors.", res[1]);
        res = ag.getResult("scissors", "rock", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Rock smashes scissors.", res[1]);
        res = ag.getResult("scissors", "paper", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Scissors cut paper.", res[1]);
        res = ag.getResult("scissors", "lizard", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Scissors decapitate lizard.", res[1]);
        res = ag.getResult("scissors", "scissors", "Bobbo", "Frank");
        assertEquals(null, res[0]);
        assertEquals("Tie!", res[1]);
        
        res = ag.getResult("rock", "spock", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Spock vaporizes rock.", res[1]);
        res = ag.getResult("rock", "paper", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Paper covers rock.", res[1]);
        res = ag.getResult("rock", "scissors", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Rock smashes scissors.", res[1]);
        res = ag.getResult("rock", "lizard", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Rock crushes lizard.", res[1]);
        res = ag.getResult("rock", "rock", "Bobbo", "Frank");
        assertEquals(null, res[0]);
        assertEquals("Tie!", res[1]);
        
        res = ag.getResult("paper", "scissors", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Scissors cut paper.", res[1]);
        res = ag.getResult("paper", "lizard", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Lizard eats paper.", res[1]);
        res = ag.getResult("paper", "rock", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Paper covers rock.", res[1]);
        res = ag.getResult("paper", "spock", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Paper disproves Spock.", res[1]);
        res = ag.getResult("paper", "paper", "Bobbo", "Frank");
        assertEquals(null, res[0]);
        assertEquals("Tie!", res[1]);
        
        res = ag.getResult("spock", "paper", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Paper disproves Spock.", res[1]);
        res = ag.getResult("spock", "lizard", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Lizard poisons Spock.", res[1]);
        res = ag.getResult("spock", "rock", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Spock vaporizes rock.", res[1]);
        res = ag.getResult("spock", "scissors", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Spock breaks scissors.", res[1]);
        res = ag.getResult("spock", "spock", "Bobbo", "Frank");
        assertEquals(null, res[0]);
        assertEquals("Tie!", res[1]);
        
        
        res = ag.getResult("lizard", "rock", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Rock crushes lizard.", res[1]);
        res = ag.getResult("lizard", "scissors", "Bobbo", "Frank");
        assertEquals("Frank", res[0]);
        assertEquals("Scissors decapitate lizard.", res[1]);
        res = ag.getResult("lizard", "spock", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Lizard poisons Spock.", res[1]);
        res = ag.getResult("lizard", "paper", "Bobbo", "Frank");
        assertEquals("Bobbo", res[0]);
        assertEquals("Lizard eats paper.", res[1]);
        res = ag.getResult("lizard", "lizard", "Bobbo", "Frank");
        assertEquals(null, res[0]);
        assertEquals("Tie!", res[1]);
	}
	
	@Test
	public void testInvalidThings() throws InterruptedException, InvalidActionException
	{
        service_.seekGame("Bobbo");
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        waitForPoll();
        ActiveGame ag = service_.activeGames().get(0);
        
        try
		{
			ag.makeChoice("Bobbo", "Gatling Gun");
			assertTrue("Gatling Gun should be invalid", false);
		} catch (InvalidActionException e)
		{
		}
        
        try
		{
			ag.makeChoice("Chewie", "spock");
			assertTrue("Chewie Gun should be invalid", false);
		} catch (InvalidActionException e)
		{
		}
        
        ag.makeChoice("Bobbo",  "spock");
        try
		{
			ag.makeChoice("Bobbo", "lizard");
			assertTrue("Should fail because Bobbo already acted", false);
		} catch (InvalidActionException e)
		{
		}
        
        ag.cancel();
        try
 		{
 			ag.makeChoice("Frank", "lizard");
 			assertTrue("Game was canceled", false);
 		} catch (InvalidActionException e)
 		{
 		}
        
        try
 		{
 			service_.makeChoice("Fahrvergnügen", "Frank", "lizard");
 			assertTrue("Should not make choice for game that doesn't exist", false);
 		} catch (InvalidActionException e)
 		{
 		}
	}
	
	private void clearMessages()
	{
		allMessages_.clear();
		lastMessage_ = null;
	}

	private class MockUserService extends BasePageUserService
	{
		Map<String, Integer> wins_ = new HashMap<String, Integer>();
		Map<String, Integer> losses_ = new HashMap<String, Integer>();
		public MockUserService()
		{
			super(null);
		}

		@Override
		public void recordWin(String user)
		{
			if (!wins_.containsKey(user))
			{
				wins_.put(user,  0);
			}
			wins_.put(user,  wins_.get(user) + 1);
		}
		
		@Override
		public void recordLoss(String user)
		{
			if (!losses_.containsKey(user))
			{
				losses_.put(user,  0);
			}
			losses_.put(user,  losses_.get(user) + 1);			
		}
		
		public int getWins(String user)
		{
			return wins_.containsKey(user) ? wins_.get(user) : 0;
		}
		
		public int getLosses(String user)
		{
			return losses_.containsKey(user) ? losses_.get(user) : 0;
		}
	}



	@Override
	public void onGameMessage(GameMessage msg)
	{
		// TODO Auto-generated method stub
		lastMessage_ = msg;
		allMessages_.add(0, msg);
		
	}
}
