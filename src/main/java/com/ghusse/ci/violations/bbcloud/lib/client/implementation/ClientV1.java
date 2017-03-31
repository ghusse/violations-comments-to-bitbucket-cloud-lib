package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v1.Comment;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ClientV1 {
  private static final String ENDPOINT = "https://api.bitbucket.org/1.0";
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientV1.class);

  private RestClient client;

  @Inject
  public ClientV1(RestClient client) {
    this.client = client;
  }

  public void setAuthentication(String userName, String password) {
    this.client.setAuthentication(userName, password);
  }

  public void publishLineComment(PullRequestDescription description,
                                 String content,
                                 String filename,
                                 int lineNumber) throws ClientException {
    Comment comment = new Comment(content, filename, lineNumber);

    this.publishComment(description, comment);
  }

  public void publishPullRequestComment(PullRequestDescription description,
                                        String content) throws ClientException {
    Comment comment = new Comment(content);

    this.publishComment(description, comment);
  }

  public void deleteComment(PullRequestDescription description, long commentId) throws ClientException {
    String url = String.format(Locale.ENGLISH,
            "%s/repositories/%s/%s/pullrequests/%s/comments/%d",
            ENDPOINT,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId(),
            commentId);

    try {
      this.client.delete(url);
    } catch (RestClientException e) {
      throw new ClientException(
              String.format(Locale.ENGLISH, "Unable to delete a comment: %d", commentId),
              description,
              e);
    }
  }

  public void publishComment(PullRequestDescription description, Comment comment) throws ClientException {
    LOGGER.debug("Publish comment {} on pull request {}", comment.getContent(), description);
    String url = String.format(Locale.ENGLISH,
            "%s/repositories/%s/%s/pullrequests/%s/comments",
            ENDPOINT,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId());

    try {
      this.client.post(url, comment);
    } catch (RestClientException e) {
      throw new ClientException(
              String.format("Unable to publish a comment: %s", comment),
              description,
              e);
    }
  }
}
