package online.cal.basePage.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.*;

import online.cal.basePage.*;
import online.cal.basePage.model.BasePageUserService.*;
import online.cal.basePage.model.GameService.*;

public class GameServiceTest implements GameListener
{

	GameService service_;
	MockUserService userService_;
	
	@Before
	public void setUp()
	{
		BasePageUserService.INSTANCE = null;
		service_ = new GameService();
		userService_ = new MockUserService();
		service_.userService_ = userService_;
		service_.addListener(this);
		service_.init();
	}
	
	@After
	public void tearDown()
	{
		service_.shutdown();
		service_ = null;
		userService_ = null;
	}
	
	@Test
	public void testMatch() throws InterruptedException
	{
        service_.seekGame("Bobbo");
        Thread.sleep(1500);
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        Thread.sleep(1500);
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
	public void testGame() throws InterruptedException, InvalidActionException
	{
        service_.seekGame("Bobbo");
        Thread.sleep(1500);
        assertEquals(service_.activeGames().size(), 0);
        service_.seekGame("Frank");
        Thread.sleep(1500);
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
		
	}
}
