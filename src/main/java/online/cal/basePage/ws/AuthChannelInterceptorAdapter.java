package online.cal.basePage.ws;

import java.util.*;

import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;

import com.auth0.jwt.exceptions.*;

import online.cal.basePage.JwtUtils.*;

@Component
public class AuthChannelInterceptorAdapter extends ChannelInterceptorAdapter {


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
			    final UsernamePasswordAuthenticationToken user = 
	            		new UsernamePasswordAuthenticationToken(
	                            authRequest.getName(),
	                            null,
	                            Collections.singleton((GrantedAuthority) () -> "USER"));

	            accessor.setUser(user);
			} catch (TokenExpiredException tee)
			{
				// Bad auth token, refuse connection
            	return null;
			}

        
        }
        return message;
    }
}