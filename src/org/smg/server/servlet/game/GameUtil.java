package org.smg.server.servlet.game;

import static org.smg.server.servlet.game.GameConstants.*;
import static org.smg.server.servlet.developer.DeveloperConstants.ACCESS_SIGNATURE;
import static org.smg.server.servlet.developer.DeveloperConstants.DEVELOPER_ID;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.smg.server.database.DatabaseDriverPlayer;
import org.smg.server.database.DeveloperDatabaseDriver;
import org.smg.server.database.models.Player;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class GameUtil {
	@SuppressWarnings("rawtypes")
	/**
	 * Check whether the accessSignature matches the userId based on the HttpRequest
	 * @param req 
	 * @return
	 */
  public static  boolean signatureRight(HttpServletRequest req)
	{
		try
		{
		  long developerId = Long.parseLong(req.getParameter(DEVELOPER_ID));
		  Map developer = DeveloperDatabaseDriver.getDeveloperMap(developerId);
		  if (req.getParameter(ACCESS_SIGNATURE).equals(developer.get(ACCESS_SIGNATURE)))
			  return true;
		  else
			return false;
		}
		catch (Exception e)
		{
			return false;
		}
		
	}
	/**
	 * Check whether the accessSignature matches the userId based on the developer parameterMap 
	 * @param parameterMap
	 * @return
	 */
	@SuppressWarnings("rawtypes")
  public static boolean signatureRight(Map<Object, Object> parameterMap) {
		try {
			long developerId = Long.parseLong((String) parameterMap
					.get(DEVELOPER_ID));
			Map developer = DeveloperDatabaseDriver
					.getDeveloperMap(developerId);
			if (parameterMap.get(ACCESS_SIGNATURE).equals(
					developer.get(ACCESS_SIGNATURE)))

				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}

	}
	/**
	 * Check whether the accessSignature matches the userId based on the player parameterMap 
	 * @param parameterMap
	 * @return
	 */
	public static boolean signatureRightForPlayer(Map<Object, Object> parameterMap) {
		try {
			long playerId = Long.parseLong((String) parameterMap
					.get(PLAYER_ID));
			Player player = DatabaseDriverPlayer.getPlayerById(playerId);
			if (parameterMap.get(ACCESS_SIGNATURE).equals(
					player.getProperty(Player.PlayerProperty.accessSignature)))

				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}

	}
	/**
	 * Put the key value pair into a HttpResponse
	 * @param jObj
	 * @param key
	 * @param value
	 * @param resp
	 */
	public  static void put (JSONObject jObj,String key,String value,HttpServletResponse resp)
	{
		try
		{
              jObj.put(key, value);
              resp.setContentType("text/plain");
              jObj.write(resp.getWriter());
		}
		catch (Exception e)
		{
			return;
		}
	}
	/**
	 * Put the JSON Object into a HttpResponse
	 * @param jObj
	 * @param resp
	 */
	public static void put(JSONObject jObj, HttpServletResponse resp)
	{
		try
		{
              resp.setContentType("text/plain");
              jObj.write(resp.getWriter());
		}
		catch (Exception e)
		{
			return;
		}
	}
	/**
	 * Wrap the list into a JSON list
	 * @param jObj
	 * @param resp
	 */
	public static void put(List<JSONObject> jObj, HttpServletResponse resp)
	{
		try
		{
		      JSONArray jsonArray = new JSONArray();
		      for (int i=0;i<jObj.size();i++)
		      {
		    	  jsonArray.put(jObj.get(i));
		      }			  
              resp.setContentType("text/plain");
              
              jsonArray.write(resp.getWriter());
		}
		catch (Exception e)
		{
			return;
		}
	}
	/**
     * Get a full request url for request object.
     * @param request GET/POST request object.
     * @return Full request URL.
     */
    public static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
}
