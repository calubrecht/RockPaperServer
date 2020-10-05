package online.cal.basePage.util;

public class Notifier
{

	public synchronized void waitForNotify() throws InterruptedException
	{
		wait();
	}
	
	public synchronized void notifyWaiters()
	{
		notifyAll();
	}
}
