package online.cal.basePage.model;

import com.fasterxml.jackson.annotation.*;

public class GameMessage
{
  @JsonProperty("id")  public String id_;
  @JsonProperty("action")  public String action_;
  @JsonProperty("detail")  public String detail_;
  public Pair<String> players_;

  public GameMessage(String id, String action, String detail, Pair<String> players)
  {
	  id_ = id;
	  action_ = action;
	  detail_ = detail;
	  players_ = players;
  }
  
  @JsonProperty("players")
  public String[] getPlayers()
  {
	  return new String[] { players_.getFirst(), players_.getSecond()};
  }
}
