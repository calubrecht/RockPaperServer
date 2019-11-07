package online.cal.basePage.ws;

import java.util.*;

import org.springframework.stereotype.*;

@Component
public class WSSessionService
{
    private HashMap<String, String> clientSessionToUser_ = new HashMap<String, String>();
    private HashMap<String, List<String>> userToClientSessions_ = new HashMap<String, List<String>>();
    
    private List<String> sessionsForUser(String userID)
    {
       	if (!userToClientSessions_.containsKey(userID))
    	{
    		userToClientSessions_.put(userID, new ArrayList<String>());
    	}
       	return userToClientSessions_.get(userID);
    }
    
    public synchronized void addUserClientSession(String userID, String sessionID)
    {
    	clientSessionToUser_.put(sessionID, userID);
    	sessionsForUser(userID).add(sessionID);
    }
    
    public synchronized List<String> getSessionIDs(String userID)
    {
    	return new ArrayList<String>(sessionsForUser(userID));
    }
    
    public synchronized void clearUser(String userID)
    {
    	for (String i : sessionsForUser(userID))
    	{
    		clientSessionToUser_.remove(i);
    	}
    	userToClientSessions_.remove(userID);
    }
}
