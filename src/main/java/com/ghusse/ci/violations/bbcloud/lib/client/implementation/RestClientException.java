package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.google.api.client.http.GenericUrl;

import java.util.Locale;

/**
 * Created by Ghusse on 26/03/2017.
 */
public class RestClientException extends Exception {
  public RestClientException(String message, String verb, GenericUrl url, int httpCode, String response){
    super(buildMessage(message, verb, url, httpCode, response));
  }

  public RestClientException(String message, String verb, GenericUrl url, int httpCode, Throwable cause){
    super(buildMessage(message, verb, url, httpCode, null), cause);
  }

  public RestClientException(String message, String verb, GenericUrl url, Throwable cause) {
    super(buildMessage(message, verb, url), cause);
  }

  private static String buildMessage(String message, String verb, GenericUrl url, int httpCode, String response){
    return String.format(Locale.ENGLISH, "%s %s", buildMessage(message, verb, url), buildMessage(httpCode, response));
  }

  private static String buildMessage(String message, String verb, GenericUrl url){
    return String.format(Locale.ENGLISH, "%s %s %s.", message, verb, url);
  }

  private static String buildMessage(int httpCode, String response){
    return String.format(Locale.ENGLISH, "Received code %d: %s", httpCode, response);
  }
}
