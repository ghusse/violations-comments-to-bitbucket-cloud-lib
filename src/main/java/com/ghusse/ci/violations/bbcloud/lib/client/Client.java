package com.ghusse.ci.violations.bbcloud.lib.client;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV1;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV2;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Ghusse on 18/03/2017.
 */
public class Client {
  private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

  private ClientV1 clientV1;
  private ClientV2 clientV2;

  @Inject
  public Client(ClientV1 clientV1, ClientV2 clientV2) {
    this.clientV1 = clientV1;
    this.clientV2 = clientV2;
  }

  public void setAuthentication(String userName, String password) {
    this.clientV1.setAuthentication(userName, password);
    this.clientV2.setAuthentication(userName, password);
  }

  public List<Comment> listCommentsForPullRequest(PullRequestDescription pullRequestDescription) throws ClientException {
    return this.clientV2.listCommentsForPullRequest(pullRequestDescription);
  }

  public void publishLineComment(PullRequestDescription pullRequestDescription,
                                 String content,
                                 String fileName,
                                 int line) throws ClientException {
    this.clientV1.publishLineComment(pullRequestDescription, content, fileName, line);
  }

  public void publishPullRequestComment(PullRequestDescription pullRequestDescription,
                                        String content) throws ClientException {
    this.clientV1.publishPullRequestComment(pullRequestDescription, content);
  }

  public void deleteComment(PullRequestDescription pullRequestDescription,
                            long commentId) throws ClientException {
    this.clientV1.deleteComment(pullRequestDescription, commentId);
  }

  public InputStream getDiff(PullRequestDescription pullRequestDescription) throws ClientException {
    return this.clientV2.getDiff(pullRequestDescription);
  }
}
