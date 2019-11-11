package online.cal.basePage.model;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.*;

@JsonInclude(Include.NON_NULL)
public class BasePageUser
{
	private String userName_;
    private String passwordHash_;
    private byte salt_[];
	private String saltString_;
	private String color_;
	private String status_;
	private long wins_;
	private long losses_;
	
	public BasePageUser()
	{
	
	}
	
	public BasePageUser(String userName, String password, String saltString)
	{
		userName_ = userName;
		passwordHash_ = password;
		saltString_ = saltString;
		salt_ = Base64.getDecoder().decode(saltString_);
	}
	
	public BasePageUser(String userName, String password)
	{
		userName_ = userName;
		passwordHash_ = password;
		saltString_ = "";
		salt_ = null;
	}
	
	public BasePageUser(BasePageUser u)
	{
		userName_ = u.userName_;
		color_ = u.color_;
		setStatus(u.getStatus());
		passwordHash_ = u.passwordHash_;
		salt_ = u.salt_;
		saltString_ = u.saltString_;
		wins_ = u.wins_;
		losses_ = u.losses_;
	}

	public boolean isLoggedIn()
	{
		return false;
	}
	
	public boolean isGuest()
	{
		return userName_.startsWith("Guest");
	}
	
	public String getUserName()
	{
		return userName_;
	}
	
	public void setUserName(String name)
	{
		userName_ = name;
	}
	
	@JsonIgnore
	public String getPassword()
	{
		return passwordHash_;
	}
	
	@JsonIgnore
	public String getPasswordSalt()
	{
		return saltString_;
	}
	
	public void setPassword(String password)
	{
		passwordHash_ = password;
	}
	
	public String getColor()
	{
		return color_;
	}
	
	public void setColor(String c)
	{
		color_ = c;
	}
	
	public static String hashPassword(String password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] bhash = factory.generateSecret(spec).getEncoded();
		return Base64.getEncoder().encodeToString(bhash);
	}
	
	public static byte[] generateSalt()
	{
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return salt;
	}
	
	public boolean validatePassword(String password)
	{
		String newHash;
		try
		{
			newHash = hashPassword(password, salt_);
		} catch (Exception e)
		{
			return false;
		}
		return newHash.contentEquals(passwordHash_);
	}

	public String getStatus()
	{
		return status_;
	}

	public void setStatus(String status)
	{
		status_ = status;
	}

	public long getWins()
	{
		return wins_;
	}

	public void setWins(long wins)
	{
		wins_ = wins;
	}
	
	public void incrementWins()
	{
		wins_++;
	}

	public long getLosses()
	{
		return losses_;
	}

	public void setLosses(long losses)
	{
		losses_ = losses;
	}
	
	public void incrementLosses()
	{
		losses_++;
	}
}
