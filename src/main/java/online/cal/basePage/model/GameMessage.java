package online.cal.basePage.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class GameMessage
{
	@JsonProperty("id")
	public String id_;
	@JsonProperty("action")
	public String action_;
	@JsonProperty("detail")
	public String detail_;
	@JsonProperty("detail2")
	public String detail2_;
	@JsonIgnore
	private Pair<String> playerPair_;
	@JsonProperty("choices")
	String[] choices_;
	@JsonProperty("winner")
	String winner_;
	@JsonProperty("scores")
	private int[] scores_;
	@JsonProperty("round")
	private int round_;
	private String[] deliverTo_;

	public GameMessage(String id, String action, String detail, Pair<String> playerPair)
	{
		id_ = id;
		action_ = action;
		detail_ = detail;
		playerPair_ = playerPair;
		if (playerPair != null)
		{
		  deliverTo_ = getPlayers();
		}
	}

	public GameMessage() {

	}

	@JsonProperty("players")
	public String[] getPlayers()
	{
		if (playerPair_ == null)
		{
			return null;
		}
		return new String[]
		{ playerPair_.first(), playerPair_.second() };
	}

	@JsonProperty("players")
	public void setPlayers(String[] players)
	{
		playerPair_ = new Pair<String>(players[0], players[1]);
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

	public String getDetail2()
	{
		return detail2_;
	}
	
	public void setDetail(String detail)
	{
	  detail_ = detail;
	}
	
	public void setDetail2(String detail2)
	{
	  detail2_ = detail2;
	}

	public int[] getScores()
	{
		return scores_;
	}

	public void setScores(int[] scores)
	{
		scores_ = scores;
	}

	public int getRound()
	{
		return round_;
	}

	public void setRound(int round)
	{
		round_ = round;
	}
	
	@JsonIgnore
	public void setDeliverTo(String[] deliverTo)
	{
		deliverTo_ = deliverTo;
	}
	
	public String[] getDeliverTo()
	{
		return deliverTo_;
	}
}
