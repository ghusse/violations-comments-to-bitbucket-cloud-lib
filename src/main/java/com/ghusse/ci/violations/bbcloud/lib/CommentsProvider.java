package com.ghusse.ci.violations.bbcloud.lib;

import com.ghusse.ci.violations.bbcloud.lib.client.Client;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.util.Optional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment provider for Bitbucket Cloud
 */
public class CommentsProvider implements se.bjurr.violations.comments.lib.model.CommentsProvider {
  private static final Logger LOG = LoggerFactory.getLogger(CommentsProvider.class);

  private final DiffParser diffParser;
  private final Client client;

  private PullRequestDescription pullRequestDescription;

  @Inject
  public CommentsProvider(Client client, DiffParser diffParser) {
    this.client = client;
    this.diffParser = diffParser;
  }

  public void init(String userName, String password, PullRequestDescription description) {
    this.client.setAuthentication(userName, password);
    this.pullRequestDescription = description;
  }

  @Override
  public void createCommentWithAllSingleFileComments(String content) {
    try {
      this.client.publishPullRequestComment(this.pullRequestDescription, content);
    } catch (ClientException e) {
      throw new CommentsProviderError("Unable to publish a pull request comment", e, this.pullRequestDescription);
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
    } catch (ClientException e) {
      throw new CommentsProviderError("Unable to publish a line comment", e, this.pullRequestDescription);
    }
  }

  @Override
  public List<Comment> getComments() {
    try {
      List<com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment> pullRequestComments = this.client.listCommentsForPullRequest(this.pullRequestDescription);

      List<Comment> result = new ArrayList<>(pullRequestComments.size());

      for (com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment comment : pullRequestComments) {
        Comment exposedComment = new Comment(
                Long.toString(comment.getId()),
                comment.getContent(),
                comment.getType(),
                new ArrayList<String>()
        );

        result.add(exposedComment);
      }

      return result;
    } catch (ClientException e) {
      throw new CommentsProviderError("Unable to get the list of comments associated to a pull request", e, this.pullRequestDescription);
    }
  }

  @Override
  public List<ChangedFile> getFiles() {
    try {
      InputStream diff = this.client.getDiff(this.pullRequestDescription);

      List<String> paths = this.diffParser.getChangedFiles(diff);

      return getChangedFiles(paths);
    } catch (ClientException e) {
      throw new CommentsProviderError("Unable to get the diff for a pull request", e, this.pullRequestDescription);
    }
  }

  private List<ChangedFile> getChangedFiles(List<String> filePaths) {
    List<ChangedFile> result = new ArrayList<>(filePaths.size());

    for(String file: filePaths){
      result.add(new ChangedFile(file, new ArrayList<String>()));
    }

    return result;
  }

  @Override
  public void removeComments(List<Comment> list) {
    try {
      for (Comment comment : list) {
        this.client.deleteComment(this.pullRequestDescription, Integer.parseInt(comment.getIdentifier()));
      }
    } catch (ClientException e) {
      LOG.error("Unable to delete comments", e);
      throw new CommentsProviderError("Unable to delete comments", e, this.pullRequestDescription);
    }
  }

  @Override
  public boolean shouldComment(ChangedFile changedFile, Integer integer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean shouldCreateCommentWithAllSingleFileComments() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean shouldCreateSingleFileComment() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<String> findCommentFormat(Violation violation) {
    throw new UnsupportedOperationException();
  }
}
