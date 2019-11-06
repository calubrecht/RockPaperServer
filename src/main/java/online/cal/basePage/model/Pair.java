package online.cal.basePage.model;

import java.util.*;

public class Pair<T> implements Iterable<T>
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	List<T> list_ = (List<T>)new ArrayList();
	
	public Pair(T one, T two)
	{
		list_.add(one);
		list_.add(two);
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
