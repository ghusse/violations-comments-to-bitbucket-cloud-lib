package com.ghusse.ci.violations.bbcloud.lib;

public class CommentsProviderError extends Error{
  private PullRequestDescription description;

  public CommentsProviderError(String message, Throwable error, PullRequestDescription description){
    super(message, error);
    this.description = description;
  }

  public PullRequestDescription getDescription(){
    return this.description;
  }

  @Override
  public String getMessage(){
    return String.format("%s pullRequest: %s", super.getMessage(), this.description);
  }
}
