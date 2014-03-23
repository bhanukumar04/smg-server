
package org.smg.server.servlet.container;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.smg.server.database.DatabaseDriver;
import org.smg.server.database.DummyDataGen;
import org.smg.server.servlet.container.GameApi.GameState;
import org.smg.server.servlet.container.GameApi.Operation;
import org.smg.server.util.CORSUtil;
import org.smg.server.util.JSONUtil;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class MatchOperationServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    DummyDataGen.addGame();
    DummyDataGen.addPlayer();
    DummyDataGen.updateMatch();
    CORSUtil.addCORSHeader(resp);
    JSONObject returnValue = new JSONObject();
    long matchId = Long.parseLong(req.getPathInfo().substring(1));
    if (!ContainerVerification.matchIdVerify(matchId)) {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_MATCH_ID);
        returnValue.write(resp.getWriter());
      } catch (JSONException e) {
      }
      return;
    }
    long playerId = Long.parseLong(req.getParameter(ContainerConstants.PLAYER_ID));
    if (!ContainerVerification.matchIdVerify(playerId)) {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_PLAYER_ID);
        returnValue.write(resp.getWriter());
      } catch (JSONException e) {
      }
      return;
    }
    String accessSignature = req.getParameter(ContainerConstants.ACCESS_SIGNATURE);
    if (!ContainerVerification.accessSignatureVerify(accessSignature, playerId)) {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_ACCESS_SIGNATURE);
        returnValue.write(resp.getWriter());
      } catch (JSONException e) {
      }
      return;
    }

    // Return the MatchInfo entity info. I don't think this is a good way. The
    // info should be generated by MatchInfo class.
    // TODO
    Entity entity = DatabaseDriver.getEntityByKey(ContainerConstants.MATCH, matchId);
    Map<String, Object> propsMap = entity.getProperties();
    for (String key : propsMap.keySet()) {
      Object val = propsMap.get(key);
      try {
        returnValue.put(key, val);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    try {
      returnValue.write(resp.getWriter());
    } catch (JSONException e) {
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    CORSUtil.addCORSHeader(resp);
    // get json string the parse to map
    BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
    String json = "";
    if (br != null) {
      json = br.readLine();
    }
    JSONObject returnValue = new JSONObject();
    if (json != null) {
      Map<String, Object> jsonMap = null;
      try {
        jsonMap = JSONUtil.parse(json);
      } catch (IOException e) {
        try {
          returnValue.put(ContainerConstants.ERROR, e.getMessage());
          returnValue.write(resp.getWriter());
        } catch (JSONException e2) {
        }
        return;
      }
      long matchId = Long.parseLong(req.getPathInfo().substring(1));
      if (!ContainerVerification.matchIdVerify(matchId)) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_MATCH_ID);
          returnValue.write(resp.getWriter());
        } catch (JSONException e) {
        }
        return;
      }
      ArrayList<Long> playerIds = (ArrayList<Long>) jsonMap.get(ContainerConstants.PLAYER_IDS);
      if (!ContainerVerification.playerIdsVerify(playerIds)) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_PLAYER_ID);
          returnValue.write(resp.getWriter());
        } catch (JSONException e) {
        }
        return;
      }
      String accessSignature = (String) jsonMap.get(ContainerConstants.ACCESS_SIGNATURE);
      if (!ContainerVerification.accessSignatureVerify(accessSignature, playerIds)) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_ACCESS_SIGNATURE);
          returnValue.write(resp.getWriter());
        } catch (JSONException e) {
        }
        return;
      }

      // From now, processing the MakeMoveRequest.
      Entity entity = DatabaseDriver.getEntityByKey(ContainerConstants.MATCH, matchId);

      // Convert the JSON string to Object.
      ObjectMapper mapper = new ObjectMapper();
      MakeMoveRequest makeMoveRequest = mapper.readValue(json, MakeMoveRequest.class);

      try {
        MatchInfo mi = MatchInfo.getMatchInfoFromEntity(entity);
        GameState currentState = mi.getGameStateHistory().get(0).getCurrentState();
        List<Operation> operations = GameStateManager.messageToOperationList(makeMoveRequest
            .getOperations());
        currentState.makeMove(operations);

        // Write the object back to JSON formation.
        String jsn = mapper.writeValueAsString(mi);

        // TODO Don't know how to convert MatchInfo object to a new JSONObject
        // or a Map<String, Object>.
        DatabaseDriver.updateMatchEntity(matchId, new HashMap<String, Object>());
      } catch (JSONException e) {
        // This will be reached if there is something wrong with the formation
        // in Entity.
        e.printStackTrace();
      }

    } else {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.NO_DATA_RECEIVED);
      } catch (JSONException e) {
      }
    }

    // write to resp then return
    try {
      returnValue.write(resp.getWriter());
    } catch (JSONException e) {
    }
  }
}
