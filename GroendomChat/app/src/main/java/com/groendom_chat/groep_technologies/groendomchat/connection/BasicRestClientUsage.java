package com.groendom_chat.groep_technologies.groendomchat.connection;

/**
 * Created by P on 18.09.2017.
 */

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class BasicRestClientUsage {

  public void getPublicTimeline() {
    BasicRestClient.get("statuses/public_timeline.json", null, new JsonHttpResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        JSONObject firstEvent = null;
        String tweetText = "";
        try {
          firstEvent = (JSONObject) response.get(0);
          tweetText = firstEvent.getString("text");
        } catch (JSONException e) {
          e.printStackTrace();
        }

        // Do something with the response
        System.out.println("asdf!:" + tweetText);
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, Throwable throwable,
          JSONObject errorResponse) {
        System.out.println("FAAAAILED");
      }
    });
  }
}
