package online.cal.basePage.ws;

import org.springframework.context.annotation.*;
import org.springframework.messaging.simp.config.*;
import org.springframework.scheduling.*;
import org.springframework.scheduling.concurrent.*;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins("*");
                //.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("")
                .enableSimpleBroker("/topic/chat","/queue")
                .setTaskScheduler(createTaskScheduler())
                .setHeartbeatValue(new long[] {10000, 10000});
            //    registry.setUserDestinationPrefix("/queue");
    }
    
    @Bean
    public TaskScheduler createTaskScheduler()
    {
    	return new ThreadPoolTaskScheduler();
    }
}