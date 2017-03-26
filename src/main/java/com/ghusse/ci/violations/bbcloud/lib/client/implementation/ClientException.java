package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;

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

  private static String buildMessage(String message, PullRequestDescription description){
    return String.format(Locale.ENGLISH, "%s Pull request in repo %s/%s id: %s",
            message,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId());
  }
}
