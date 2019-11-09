package online.cal.basePage.model;

import com.fasterxml.jackson.annotation.*;

public class GameMessage
{
  @JsonProperty("id")  public String id_;
  @JsonProperty("action")  public String action_;
  @JsonProperty("detail")  public String detail_;
  @JsonIgnore public Pair<String> players_;
  @JsonProperty("choices") String[] choices_;
  @JsonProperty("winner") String winner_;

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
  
  public void setChoices(String[] choices)
  {
	  choices_ = choices;
  }
  
  public void setWinner(String winner)
  {
	  winner_ = winner;
  }
  
  public String getGameID()
  {
	  return id_;
  }
  
  public String getAction()
  {
	  return action_;
  }
  
  public String getDetail()
  {
	  return detail_;
  }
}
