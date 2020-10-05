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
		
		assertEquals("First", p.getFirst());
		assertEquals("Second", p.getSecond());
	}
	
	@Test
	public void testIterator()
	{
		Pair<Integer> p = new Pair<>(new Integer[] {3, 21});
		
		List<Integer> l = new ArrayList<>();
		for (Integer i : p)
		{
			l.add(i);
		}
		
		assertEquals(3, l.get(0).intValue());
		assertEquals(21, l.get(1).intValue());
		
	}
}
