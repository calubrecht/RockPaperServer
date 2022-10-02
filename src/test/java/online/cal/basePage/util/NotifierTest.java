package online.cal.basePage.util;

import static org.junit.Assert.assertTrue;

import org.junit.*;

public class NotifierTest
{

	// No need to run test, because GameService uses this and test shows it's working
	//@Test
	//@Ignore // Ignore. The point of the Notifier object is to prevent the need to wait, to speed up tests.
	public void testNotifier() throws InterruptedException
	{
		Notifier notify = new Notifier();
		
		final boolean[] threadDone = {false};
		
		Thread t = new Thread(() ->
		{
			while(true)
			{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
			}
			threadDone[0] = true;
			notify.notifyWaiters();
			}
		});
		
		t.start();
		notify.waitForNotify();
		assertTrue(threadDone[0]);
		threadDone[0] = false;
		notify.waitForNotify();
		assertTrue(threadDone[0]);
		
	}
}
