package online.cal.basePage.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import online.cal.basePage.*;

@RestController
@RequestMapping(AppConstants.API_PATH)
public class VersionController
{
    public static VersionController INSTANCE;
    
    public VersionController()
    {
       INSTANCE = this;
    }
    
	@Value ( "${app.version}")
	private String appVersion;
	
	@RequestMapping("version")
	public String version()
	{
	  return appVersion;
	}
	
	public static String getVersion()
	{
	  return INSTANCE.appVersion;
	}
}
