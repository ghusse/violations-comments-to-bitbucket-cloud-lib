package com.ghusse.ci.violations.bbcloud.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghusse.ci.violations.bbcloud.lib.client.Client;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV1;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV2;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.JsonHttpContentFactory;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClient;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.inject.Inject;

import io.reflectoring.diffparser.api.UnifiedDiffParser;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;
import io.reflectoring.diffparser.api.model.Line;
import io.reflectoring.diffparser.api.model.Line.LineType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment provider for Bitbucket Cloud
 */
public class CommentsProvider implements se.bjurr.violations.comments.lib.model.CommentsProvider {
  private static final Logger LOG = LoggerFactory.getLogger(CommentsProvider.class);

  private final DiffParser diffParser;
  private final UnifiedDiffParser uniDiff;
  private final Client client;
  private ViolationCommentsToBitbucketCloudApi api;
  private Integer linesCommented = 0;

  private PullRequestDescription pullRequestDescription;

  @Inject
  public CommentsProvider(ViolationCommentsToBitbucketCloudApi api, Client client, DiffParser diffParser, UnifiedDiffParser uniDiff) {
    this.client = client;
    this.diffParser = diffParser;
    this.uniDiff = uniDiff;
    this.api = api;
  }

  public CommentsProvider(ViolationCommentsToBitbucketCloudApi violationCommentsToBitbucketApi) {

    ObjectMapper mapper = new ObjectMapper();
    JsonHttpContentFactory contentFactory = new JsonHttpContentFactory(mapper);
    NetHttpTransport transport = new NetHttpTransport();

    RestClient restClient = new RestClient(transport, contentFactory);
    ClientV1 clientV1 = new ClientV1(restClient);
    ClientV2 clientV2 = new ClientV2(restClient, mapper);
    this.client = new Client(clientV1, clientV2);
    this.diffParser = new DiffParser();
    this.uniDiff = new UnifiedDiffParser();

    PullRequestDescription description =
        new PullRequestDescription(
            this.api.getProjectKey(),
            this.api.getRepoSlug(),
            String.valueOf(this.api.getPullRequestId()));
    this.init(this.api.getUsername(), this.api.getPassword(), this.api, description);
  }

  public void init(String userName, String password, ViolationCommentsToBitbucketCloudApi violationCommentsToBitbucketApi, PullRequestDescription description) {
    this.api = violationCommentsToBitbucketApi;
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
      LOG.debug("Get files");
      InputStream diff = this.client.getDiff(this.pullRequestDescription);

      List<String> paths = this.diffParser.getChangedFiles(diff);

      return getChangedFiles(paths);
    } catch (ClientException e) {
      LOG.error("Unable to retrieve files");
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
  public boolean shouldComment(ChangedFile changedFile, Integer changedLine) {
    if (!api.getCommentOnlyChangedContent()) {
      return true;
    }
    InputStream diff;
    try {
      LOG.debug("Should comment " + changedFile.getFilename() + ":" +changedLine);
      diff = this.client.getDiff(this.pullRequestDescription);

      final int context = api.getCommentOnlyChangedContentContext();
      final List<Diff> diffs = uniDiff.parse(diff);
      boolean shouldComment = shouldComment(changedFile, changedLine, context, diffs);

      if (shouldComment) {
        LOG.debug("  YES");
        this.linesCommented++;
      } else {
        LOG.debug("  NO");
      }

      return shouldComment;
    } catch (ClientException e) {
      LOG.debug("  Client Error");
      e.printStackTrace();
      return false;
    }
  }

  boolean shouldComment(ChangedFile changedFile, Integer changedLine, int context, List<Diff> diffs) {
    for (final Diff diff : diffs) {
      final String destinationToString = diff.getToFileName().substring(2);
      if (!isNullOrEmpty(destinationToString)) {
        if (destinationToString.equals(changedFile.getFilename())) {
          if (diff.getHunks() != null) {
            for (final Hunk hunk : diff.getHunks()) {
              Integer lineStart = hunk.getToFileRange().getLineStart();
              Integer position = lineStart - 1;
              for (Line line : hunk.getLines()) {
                if (LineType.NEUTRAL.equals(line.getLineType())) {
                  position += 1;
                } else if (LineType.TO.equals(line.getLineType())) {
                  position += 1;
                  if (position >= changedLine - context && position <= changedLine + context) {
                    return true;
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean shouldCreateCommentWithAllSingleFileComments() {
    return this.api.getCreateCommentWithAllSingleFileComments();
  }

  @Override
  public boolean shouldCreateSingleFileComment() {
    return this.api.getCreateSingleFileComments();
  }

  @Override
  public Optional<String> findCommentFormat(ChangedFile arg0, Violation arg1) {
    return Optional.absent();
  }

  @Override
  public boolean shouldKeepOldComments() {
    return this.api.getShouldKeepOldComments();
  }

public Integer getLinesCommented() {
    return linesCommented;
}

public void setLinesCommented(Integer linesCommented) {
    this.linesCommented = linesCommented;
}
}
