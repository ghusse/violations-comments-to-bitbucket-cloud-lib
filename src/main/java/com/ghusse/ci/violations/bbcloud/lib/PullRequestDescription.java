package com.ghusse.ci.violations.bbcloud.lib;

import java.io.Serializable;
import java.util.Locale;

public class PullRequestDescription implements Serializable {
  private final String userName;
  private final String repositorySlug;
  private final String id;

  public PullRequestDescription(String userName, String repositorySlug, String id) {
    this.userName = userName;
    this.repositorySlug = repositorySlug;
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public String getRepositorySlug() {
    return repositorySlug;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString(){
    return String.format(Locale.ENGLISH, "user:%s repo:%s id:%s", this.getUserName(), this.getRepositorySlug(), this.getId());
  }
}
