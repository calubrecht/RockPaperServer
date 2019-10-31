package online.cal.basePage.controller;
import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.jetty.http.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class SimpleController implements ErrorController
{
	@RequestMapping("/")
	public String home()
	{
		return "Boot!";
	}
	
	@RequestMapping("/lyrics")
	public String lyrics()
	{
		return wrapHTML("<p>Give 'em the <b style='color:#ff0000'>BOOT</b>! The roots, the radicals</p>");
	}
	
	@RequestMapping("/test")
	public String test()
	{
		return wrapHTML("<p>Should see this</p>");
	}
	
	private String wrapHTML(String body)
	{
		return "<html><head><title>Boot</title></head><body>" + body + "</body></html>";
	}
	
	@RequestMapping("/error")
	public String handleError(HttpServletRequest request)
	{
	  Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
	  if (status != null)
	  {
		  Integer statusCode = Integer.valueOf(status.toString());
		  if (statusCode == HttpStatus.NOT_FOUND_404)
		  {
			  return "error-404";
		  }
		  if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR_500)
		  {
			  return "error-500";
		  }
		  if (statusCode == HttpStatus.UNAUTHORIZED_401)
		  {
			  return "error-401 - Who are you?";
		  }
          return "error-" + status;
	  }
	  return "error";
	}
	
	@Override
	public String getErrorPath()
	{
		return "/error";
	}
}
