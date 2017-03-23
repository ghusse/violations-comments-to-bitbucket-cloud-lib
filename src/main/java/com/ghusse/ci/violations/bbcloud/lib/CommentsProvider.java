package com.ghusse.ci.violations.bbcloud.lib;

import com.ghusse.ci.violations.bbcloud.lib.client.Client;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.util.Optional;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment provider for Bitbucket Cloud
 */
public class CommentsProvider implements se.bjurr.violations.comments.lib.model.CommentsProvider {
  private static final Logger LOG = LoggerFactory.getLogger(CommentsProvider.class);

  @Inject
  private Client client;

  private PullRequestDescription pullRequestDescription;

  public CommentsProvider() {
  }

  public void init(String userName, String password, PullRequestDescription description) {
    this.client.setAuthentication(userName, password);
    this.pullRequestDescription = description;
  }

  @Override
  public void createCommentWithAllSingleFileComments(String content) {
    try {
      this.client.publishPullRequestComment(this.pullRequestDescription, content);
    } catch (IOException e) {
      LOG.error("Unable to publish a pull request comment", e);
      throw new RuntimeException("Unable to publish a pull request comment", e);
    }
  }

  @Override
  public void createSingleFileComment(ChangedFile changedFile, Integer lineNumber, String content) {
    try {
      this.client.publishLineComment(
              this.pullRequestDescription,
              content,
              changedFile.getFilename(),
              lineNumber);
    } catch (IOException e) {
      LOG.error("Unable to publish a line comment", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Comment> getComments() {
    try {
      List<com.ghusse.ci.violations.bbcloud.lib.client.model.V2.Comment> pullRequestComments = this.client.listCommentsForPullRequest(this.pullRequestDescription);

      List<Comment> result = new ArrayList<>(pullRequestComments.size());

      for (com.ghusse.ci.violations.bbcloud.lib.client.model.V2.Comment comment : pullRequestComments) {
        Comment exposedComment = new Comment(
                Long.toString(comment.getId()),
                comment.getContent(),
                comment.getType(),
                new ArrayList<String>()
        );

        result.add(exposedComment);
      }

      return result;
    } catch (IOException e) {
      LOG.error("Unable to get the list of comments associated to a pull request", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ChangedFile> getFiles() {
    throw new NotImplementedException();
  }

  @Override
  public void removeComments(List<Comment> list) {
    try {
      for (Comment comment : list) {
        this.client.deleteComment(this.pullRequestDescription, Integer.parseInt(comment.getIdentifier()));
      }
    } catch (IOException e) {
      LOG.error("Unable to delete comments", e);
      throw new RuntimeException("Unable to delete comments", e);
    }
  }

  @Override
  public boolean shouldComment(ChangedFile changedFile, Integer integer) {
    throw new NotImplementedException();
  }

  @Override
  public boolean shouldCreateCommentWithAllSingleFileComments() {
    throw new NotImplementedException();
  }

  @Override
  public boolean shouldCreateSingleFileComment() {
    throw new NotImplementedException();
  }

  @Override
  public Optional<String> findCommentFormat(Violation violation) {
    throw new NotImplementedException();
  }
}
