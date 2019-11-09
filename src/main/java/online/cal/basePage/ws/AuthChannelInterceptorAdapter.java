package online.cal.basePage.ws;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;

import com.auth0.jwt.*;
import com.auth0.jwt.exceptions.*;

import online.cal.basePage.JwtUtils.*;

@Component
public class AuthChannelInterceptorAdapter extends ChannelInterceptorAdapter {

	@Autowired
	WSSessionService sessionService_;

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
				JwtAuthenticationToken authRequest = new JwtAuthenticationToken(authToken);

				authRequest.validate();
				String userName = authRequest.getName();
			    final UsernamePasswordAuthenticationToken user = 
	            		new UsernamePasswordAuthenticationToken(
	            				userName,
	                            null,
	                            Collections.singleton((GrantedAuthority) () -> "USER"));

	            accessor.setUser(user);
	            String clientSessionID = accessor.getFirstNativeHeader("ClientSessionID");
	            sessionService_.addUserClientSession(userName, clientSessionID);
	            System.out.println(
	    	    		"WS connect from user " + userName  + "-" + clientSessionID + "-" + accessor.getSessionId()
	    	    		+" on " +
	    	    		accessor.getNativeHeader("destination") + "-" + accessor.getHeader("lookupDestination"));
			} catch (TokenExpiredException tee)
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
        	// remove user sessions from WSSessionService?
 
        }
        return message;
    }
}