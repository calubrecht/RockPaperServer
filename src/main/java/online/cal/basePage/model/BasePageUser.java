package online.cal.basePage.model;

public class BasePageUser
{
	private String userName_;
	private String password_;
	
	public BasePageUser()
	{
	
	}
	
	public BasePageUser(String userName, String password)
	{
		userName_ = userName;
		password_ = password;
	}

	public boolean isLoggedIn()
	{
		return false;
	}
	
	public String getUserName()
	{
		return userName_;
	}
	
	public void setUserName(String name)
	{
		userName_ = name;
	}
	
	public String getPassword()
	{
		return password_;
	}
	
	public void setPassword(String password)
	{
		password_ = password;
	}
}
