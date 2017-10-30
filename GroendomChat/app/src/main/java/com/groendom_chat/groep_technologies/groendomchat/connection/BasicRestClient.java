package com.groendom_chat.groep_technologies.groendomchat.connection;

/**
 * Created by P on 18.09.2017.
 */

import com.loopj.android.http.*;

class BasicRestClient {

  private static final String BASE_URL = "https://api.twitter.com/1/";

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String url, RequestParams params,
      AsyncHttpResponseHandler responseHandler) {
    client.get(getAbsoluteUrl(url), params, responseHandler);
  }

  public static void post(String url, RequestParams params,
      AsyncHttpResponseHandler responseHandler) {
    client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
    return BASE_URL + relativeUrl;
  }
}
