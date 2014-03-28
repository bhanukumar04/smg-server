
package org.smg.server.servlet.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.smg.server.database.DatabaseDriver;
import org.smg.server.servlet.container.GameApi.EndGame;
import org.smg.server.servlet.container.GameApi.GameState;
import org.smg.server.servlet.container.GameApi.Operation;
import org.smg.server.servlet.container.GameApi.SetTurn;
import org.smg.server.util.CORSUtil;
import org.smg.server.util.JSONUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class MatchOperationServlet extends HttpServlet {
  @Override
  public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CORSUtil.addCORSHeader(resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    CORSUtil.addCORSHeader(resp);
    JSONObject returnValue = new JSONObject();
    long matchId = 0;
    try {
      matchId = Long.parseLong(req.getPathInfo().substring(1));
    } catch (Exception e) {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_MATCH_ID);
        returnValue.write(resp.getWriter());
      } catch (JSONException e1) {
      }
      return;
    }
    if (!ContainerVerification.matchIdVerify(matchId)) {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_MATCH_ID);
        returnValue.write(resp.getWriter());
      } catch (JSONException e) {
      }
      return;
    }

    long playerId = 0;
    try {
      playerId = Long.parseLong(req.getParameter(ContainerConstants.PLAYER_ID));
    } catch (Exception e) {
      try {
        returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_PLAYER_ID);
        returnValue.write(resp.getWriter());
      } catch (JSONException e1) {
      }
      return;
    }
    if (!ContainerVerification.playerIdVerify(playerId)) {
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
    String json = Utils.getBody(req);
    JSONObject returnValue = new JSONObject();
    if (json != null && !json.isEmpty()) {
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

      ArrayList<Long> playerIds = (ArrayList<Long>) jsonMap.get(ContainerConstants.PLAYER_IDS);
      if (!ContainerVerification.playerIdsVerify(playerIds)) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_PLAYER_ID);
          returnValue.write(resp.getWriter());
        } catch (JSONException e) {
        }
        return;
      }
      String accessSignature = String.valueOf(jsonMap.get(ContainerConstants.ACCESS_SIGNATURE));
      if (!ContainerVerification.accessSignatureVerify(accessSignature, playerIds)) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_ACCESS_SIGNATURE);
          returnValue.write(resp.getWriter());
        } catch (JSONException e) {
        }
        return;
      }
      long matchId = 0;
      try {
        matchId = Long.parseLong(req.getPathInfo().substring(1));
      } catch (Exception e) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_MATCH_ID);
          returnValue.write(resp.getWriter());
        } catch (JSONException e1) {
        }
        return;
      }
      if (!ContainerVerification.matchIdVerify(matchId)) {
        try {
          returnValue.put(ContainerConstants.ERROR, ContainerConstants.WRONG_MATCH_ID);
          returnValue.write(resp.getWriter());
        } catch (JSONException e) {
        }
        return;
      }
      // Get entity for MatchInfo from database.
      Entity entity = DatabaseDriver.getEntityByKey(ContainerConstants.MATCH, matchId);

      List<Object> operations = (List<Object>) jsonMap.get(ContainerConstants.OPERATIONS);

      boolean isGameEnd = false;
      // If the game is "turn" based, nextMovePlayerId will never be -1.
      long nextMovePlayerId = -1;
      for (Object op : operations) {
        if (op instanceof EndGame) {
          isGameEnd = true;
        } else if (op instanceof SetTurn) {
          nextMovePlayerId = Long.parseLong((String) ((SetTurn) op).getPlayerId());
        }
      }

      try {
        MatchInfo mi = MatchInfo.getMatchInfoFromEntity(entity);

        // TODO This needs to be modified at first place?
        mi.setPlayerThatHasTurn(nextMovePlayerId);

        GameState newState = updateMatchInfoByOperations(mi, operations);

        // Write the object back to JSON formation.
        String rtnJsn = new ObjectMapper().writeValueAsString(mi);
        DatabaseDriver.updateMatchEntity(matchId, Utils.toMap(new JSONObject(rtnJsn)));

        // Response
        String rtnStr = new ObjectMapper().writeValueAsString(newState
            .getStateForPlayerId(String.valueOf(nextMovePlayerId)));
        returnValue.put(ContainerConstants.GAME_STATE, new JSONObject(rtnStr));

        // TODO If game is ended. Do update things.
        if (isGameEnd) {
          mi.setGameOverReason(ContainerConstants.OVER);

        }

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
    try {
      returnValue.write(resp.getWriter());
    } catch (JSONException e) {
    }
  }

  private GameState updateMatchInfoByOperations(MatchInfo mi, List<Object> operationsMapList) {
    List<Operation> operations = GameStateManager.messageToOperationList(operationsMapList);

    // There is only one history record here.
    // TODO Make sure which one will be the lastest state.
    GameState currentState;
    if (mi.getHistory().size() == 0) {
      // There is not GameState in History. Initial move.
      currentState = new GameState();

      GameStateHistoryItem gshi = new GameStateHistoryItem();
      gshi.setGameState(currentState);

      mi.getHistory().add(gshi);
    } else {
      currentState = mi.getHistory().get(0).getGameState();
    }
    currentState.makeMove(operations);
    return currentState;
  }
}
