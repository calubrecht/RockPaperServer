package online.cal.basePage.model;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.*;

public class PairTest
{

	@Test
	public void testGetters()
	{
		Pair<String> p = new Pair<>("First", "Second");
		
		assertEquals("First", p.first());
		assertEquals("Second", p.second());
	}
}
