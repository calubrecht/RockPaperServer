package online.cal.basePage.util;

public class SilentSleeper {
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			//  Who cares?
		}
		
	}

}
