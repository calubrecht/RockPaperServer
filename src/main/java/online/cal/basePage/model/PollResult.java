package online.cal.basePage.model;

public class PollResult<T>
{
	private long mostRecentData_;
	private T data_;

	public PollResult(long mostRecentData, T data)
	{
		mostRecentData_ = mostRecentData;
		setData(data);
	}

	public long getMostRecentData()
	{
		return mostRecentData_;
	}

	public void setMostRecentData(long mostRecentData)
	{
		mostRecentData_ = mostRecentData;
	}

	public T getData()
	{
		return data_;
	}

	public void setData(T data)
	{
		data_ = data;
	}
}
