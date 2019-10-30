package online.cal.basePage;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class DeployConfiguration
{
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins(getGUIDeployURL());
            }
        };
    }
    
    
    public String getGUIDeployURL()
    {
    	return "http://triklops:4200";
    }
}