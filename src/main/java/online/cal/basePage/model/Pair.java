package online.cal.basePage.model;

import java.util.*;

public class Pair<T> implements Iterable<T>
{
	List<T> list_ = new ArrayList<>();
	
	public Pair(T one, T two)
	{
		list_.add(one);
		list_.add(two);
	}
	
	public Pair(T[] values)
	{
		list_.add(values[0]);
		list_.add(values[1]);
	}
	
	public T getFirst()
	{
        return list_.get(0);
	}
	
	public T getSecond()
	{
        return list_.get(1);
	}

	@Override
	public Iterator<T> iterator()
	{
		return list_.iterator();
	}
}
