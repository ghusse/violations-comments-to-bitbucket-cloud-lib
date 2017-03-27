package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.model.V1.Comment;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ClientV1 {
  private static final String ENDPOINT = "https://api.bitbucket.org/1.0";
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientV1.class);

  @Inject
  private RestClient client;

  public ClientV1() {
  }

  public void setAuthentication(String userName, String password) {
    this.client.setAuthentication(userName, password);
  }

  public void publishLineComment(PullRequestDescription description,
                                 String content,
                                 String filename,
                                 int lineNumber) throws RestClientException {
    Comment comment = new Comment(content, filename, lineNumber);

    this.publishComment(description, comment);
  }

  public void publishPullRequestComment(PullRequestDescription description,
                                        String content) throws RestClientException {
    Comment comment = new Comment(content);

    this.publishComment(description, comment);
  }

  public void deleteComment(PullRequestDescription description, long commentId) throws RestClientException {
    String url = String.format(Locale.ENGLISH,
            "%s/repositories/%s/%s/pullrequests/%s/comments/%d",
            ENDPOINT,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId(),
            commentId);

    this.client.delete(url);
  }

  public void publishComment(PullRequestDescription description, Comment comment) throws RestClientException {
    LOGGER.debug("Publish comment {} on pull request {}", comment.getContent(), description);
    String url = String.format(Locale.ENGLISH,
            "%s/repositories/%s/%s/pullrequests/%s/comments",
            ENDPOINT,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId());

    this.client.post(url, comment);
  }
}
