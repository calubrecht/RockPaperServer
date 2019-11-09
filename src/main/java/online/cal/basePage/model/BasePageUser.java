package online.cal.basePage.model;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

public class BasePageUser
{
	private String userName_;
	private String passwordHash_;
	private byte salt_[];
	private String saltString_;
	
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
		return passwordHash_;
	}
	
	public String getPasswordSalt()
	{
		return saltString_;
	}
	
	public void setPassword(String password)
	{
		passwordHash_ = password;
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
}
