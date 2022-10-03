package online.cal.basePage.ws;

import java.util.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;

import com.auth0.jwt.exceptions.*;

import online.cal.basePage.*;
import online.cal.basePage.JwtUtils.*;
import online.cal.basePage.model.*;

@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
    Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	WSSessionService sessionService_;
	
	@Autowired WSController wsController_;
	@Autowired BasePageUserService userService_;
	@Autowired JwtUtils jwtUtils_;

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            final String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null)
            {
            	// No auth token, refuse connection
            	return null;
            }

    		String authToken = token.substring(7);

			try
			{
				JwtAuthenticationToken authRequest = new JwtAuthenticationToken(authToken, jwtUtils_);

				String userName = authRequest.getName();
			    final UsernamePasswordAuthenticationToken user = 
	            		new UsernamePasswordAuthenticationToken(
	            				userName,
	                            null,
	                            Collections.singleton((GrantedAuthority) () -> "USER"));

	            accessor.setUser(user);
	            String clientSessionID = accessor.getFirstNativeHeader("ClientSessionID");
	            sessionService_.addUserClientSession(userName, clientSessionID);
	            userService_.onConnect(userName, clientSessionID);
	            logger.info(
	    	    		"WS connect from user " + userName  + "-" + clientSessionID + "-" + accessor.getSessionId()
	    	    		+" on " +
	    	    		accessor.getNativeHeader("destination") + "-" + accessor.getHeader("lookupDestination"));
			} catch (AuthenticationException tee)
			{
				// Bad auth token, refuse connection
            	return null;
			}
			catch (SignatureVerificationException sve)
			{
				return null;
			}
			catch (JWTDecodeException jde)
			{
				// Bad auth token, refuse connection
				return null;
			}

        
        }
        if (StompCommand.DISCONNECT == accessor.getCommand())
        {
        	if (accessor.getUser() == null)
        	{
        		return message;
        	}
        	String userName = accessor.getUser().getName();
        	String clientSessionID = accessor.getFirstNativeHeader("ClientSessionID");
        	userService_.onDisconnect(userName, clientSessionID);
 
        }
        return message;
    }
}