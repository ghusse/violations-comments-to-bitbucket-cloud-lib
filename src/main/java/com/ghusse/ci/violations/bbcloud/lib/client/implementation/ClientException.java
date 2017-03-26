package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by Ghusse on 27/03/2017.
 */
public class ClientException extends Exception {
  public ClientException(Throwable cause){
    super(cause);
  }

  public ClientException(String message, PullRequestDescription description, Throwable cause){
    super(buildMessage(message, description), cause);
  }

  public ClientException(String message, PullRequestDescription description, InputStream response, IOException cause) {
    super(buildMessage(message, description, response), cause);
  }

  private static String buildMessage(String message, PullRequestDescription description, InputStream response){
    return String.format("%s. Response: %s", buildMessage(message, description), convertStreamToString(response));
  }

  private static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  private static String buildMessage(String message, PullRequestDescription description){
    return String.format(Locale.ENGLISH, "%s Pull request in repo %s/%s id: %s",
            message,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId());
  }
}
