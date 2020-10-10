package online.cal.basePage.model;

import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.function.*;

import javax.annotation.*;

import org.bson.Document;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;

import com.fasterxml.jackson.annotation.*;
import com.mongodb.client.*;

import online.cal.basePage.*;

@Component
public class BasePageUserService
{
	static BasePageUserService INSTANCE;
	Map<String, BasePageUser> users_ = new HashMap<String, BasePageUser>();
	Map<String, String> userStatuses_ = new HashMap<String, String>();
	List<UserListener> listeners_ = new ArrayList<UserListener>();

	Timer delayTimer_ = new Timer();

	@Autowired
	DBStore dbStore_;
	@Autowired
	ChatStore chatStore_;
	
	Function<String, String> tokenGenerator_ = userName -> JwtUtils.generateToken(userName);

	int guestCount = 0;

	public static BasePageUserService getService()
	{
		assert INSTANCE != null;
		return INSTANCE;
	}
	
	public BasePageUserService(DBStore dbStore, ChatStore chatStore)
	{
		dbStore_ = dbStore;
	    chatStore_ = chatStore;
	}

	@PostConstruct
	public void init()
	{
		MongoIterable<Document> itr = dbStore_.getCollection("users").find();
		for (Document d : itr)
		{
			String name = d.getString("userName");
			String passwordHash = d.getString("passwordHash");
			String passwordSalt = d.getString("passwordSalt");
			Long wins = d.getLong("wins");
			Long losses = d.getLong("losses");
			BasePageUser u = new BasePageUser(name, passwordHash, passwordSalt);
			u.setColor(d.getString("color"));
			if (wins != null)
			{
				u.setWins(wins);
			}
			if (losses != null)
			{
				u.setLosses(losses);
			}
			users_.put(name, u);
		}
	}

	public UserMessage register(BasePageUser user)
	{
		String userName = user.getUserName();
		if (userName.contains("#") || userName.startsWith("Guest") || isAI(userName))
		{
			throw new BPUAuthenticationException("Invalid username");
		}
		if (users_.containsKey(userName))
		{
			throw new BPUAuthenticationException("User already exists");
		}
		String password = user.getPassword();
		String color = user.getColor();
		
		try
		{
			BasePageUser newUser = generateUser(userName, password);
			newUser.setColor(color);
			storeUser(newUser);
		
			
			
			users_.put(userName,  newUser);
			String tok = tokenGenerator_.apply(userName);
			return new UserMessage(userName, tok);
			
		}
		catch (Exception e)
		{
			throw new BPUAuthenticationException(e.getMessage());
		}
		

	}
	
	protected BasePageUser generateUser(String userName, String plaintextPassword) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		byte[] salt = BasePageUser.generateSalt();
		String hash = BasePageUser.hashPassword(plaintextPassword, salt);
		String saltString = Base64.getEncoder().encodeToString(salt);
		BasePageUser newUser = new BasePageUser(userName, hash, saltString);
		return newUser;
	}
	
	private void storeUser(BasePageUser user)
	{
		if (user.isGuest())
		{
			return;
		}
		Document doc = new Document();
		doc.put("userName", user.getUserName());
		doc.put("color", user.getColor());
		doc.put("passwordHash", user.getPassword());
		doc.put("passwordSalt", user.getPasswordSalt());
		doc.put("wins", user.getWins());
		doc.put("losses", user.getLosses());
		dbStore_.insertOne("users", doc);
	}
	
	private void updateUser(BasePageUser user, Document update)
	{
		if (user.isGuest())
		{
			return;
		}
		Document query = new Document().append("userName", user.getUserName());
		dbStore_.update("users", query, update);
	}

	public String createGuest()
	{
		String name = "Guest-" + ++guestCount;
	    
		
		users_.put(name, createGuestUser(name));

		return name;
	}
	
	private BasePageUser createGuestUser(String name)
	{
		if (name.startsWith("Guest-"))
		{
			int index = Integer.parseInt(name.substring(6));
			if (index > guestCount)
			{
				guestCount = index;
			}
		}
		BasePageUser bpu = new BasePageUser(name, "");
		String[] colors = {"orange", "yellow", "white", "black", "blue", "green"};
		String color = colors[guestCount % colors.length];
		bpu.setColor(color);
		return bpu;
	}
	
	public static boolean isAI(String username)
	{
		return username.startsWith("AI -");
	}

	public BasePageUser getUser(String name)
	{
		return users_.get(name);
	}
	
	private String getStatus(String name)
	{
		String currStatus = userStatuses_.get(name);
		if ("CONNECTED".equals(currStatus) || "DISCONNECTING".equals(currStatus))
		{
			return "CONNECTED";
		}
		return "DISCONNECTED";
	}
	
	public void recordWin(String userName)
	{
		if (isAI(userName))
		{
			return;
		}
		BasePageUser bpu = users_.get(userName);
		bpu.incrementWins();
		updateUser(bpu, new Document().append("wins", bpu.getWins()));
		fireListeners(addStatus(bpu));
	}
	
	public void recordLoss(String userName)
	{
		if (isAI(userName))
		{
			return;
		}
		BasePageUser bpu = users_.get(userName);
		bpu.incrementLosses();
		updateUser(bpu, new Document().append("losses", bpu.getLosses()));
		fireListeners(addStatus(bpu));
	}
	
	public List<BasePageUser> getUsers()
	{
		List<BasePageUser> users = new ArrayList<BasePageUser>(users_.size());
		users_.values().forEach(
				u -> { 
					BasePageUser copy = new BasePageUser(u);
					copy.setStatus(getStatus(u.getUserName()));
					users.add(copy);});
		return users;
		
	}

	public static class BPUAuthenticationException extends AuthenticationException
	{
		public BPUAuthenticationException(String err)
		{
			super(err);
		}
	}
	
	private BasePageUser addStatus(BasePageUser user)
	{
		BasePageUser temp = new BasePageUser(user);
		temp.setStatus(userStatuses_.get(temp.getUserName()));
		return temp;
	}

	public synchronized void onConnect(String userName, String clientSesssionID)
	{
		String currStatus = userStatuses_.get(userName);
		if (currStatus == null || currStatus.equals("DISCONNECTED"))
		{
			chatStore_.sendSystemMessage(userName + " has joined.");
		}
		userStatuses_.put(userName, "CONNECTED");
		if (!users_.containsKey(userName))
		{
			// Pre-existing guest from previous run?
			users_.put(userName, createGuestUser(userName));
		}
		final BasePageUser bpu = addStatus(users_.get(userName));
		fireListeners(bpu);
		// Resend after delay, so user can pick up their own user.
		delayTimer_.schedule(new TimerTask() {

			@Override
			public void run()
			{
			//	fireListeners(bpu);

			}
		}, 2000);
		
	}

	public synchronized void onDisconnect(String userName, String clientSessionID)
	{
		if (userStatuses_.get(userName).equals("DISCONNECTING"))
		{
			return;
		}
		userStatuses_.put(userName, "DISCONNECTING");
		// If after 2 seconds, user hasn't reconnected, send message to everyone
		delayTimer_.schedule(new TimerTask() {

			@Override
			public void run()
			{
				checkDisconnect(userName, clientSessionID);

			}
		}, 2000);

	}


	public synchronized void checkDisconnect(String userName, String clientSessionID)
	{
		String currStatus = userStatuses_.get(userName);
		if (!currStatus.equals("CONNECTED"))
		{
			userStatuses_.put(userName, "DISCONNECTED");
			chatStore_.sendSystemMessage(userName + " has left.");
			fireListeners(addStatus(users_.get(userName)));
		}
	}

	
	void fireListeners(BasePageUser user)
	{
	  listeners_.forEach(l -> l.onUser(user));
	}
	
	
	public void addListener(UserListener listener)
	{
		listeners_.add(listener);
	}
	
	public static class UserMessage
	{
		@JsonProperty("userName")
		public String userName_;
		@JsonProperty("token")
		public String token_;

		public UserMessage(String userName, String token)
		{
			userName_ = userName;
			token_ = token;
		}
	}
	
	public static interface UserListener
	{
		public void onUser(BasePageUser user);
	}
}
