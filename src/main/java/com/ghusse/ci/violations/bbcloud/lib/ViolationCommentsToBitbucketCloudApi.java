package com.ghusse.ci.violations.bbcloud.lib;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static se.bjurr.violations.comments.lib.CommentsCreator.createComments;

import java.util.List;

import se.bjurr.violations.lib.model.Violation;

public class ViolationCommentsToBitbucketCloudApi {
  private static final Integer BITBUCKET_MAX_COMMENT_SIZE = 32767;

  public static ViolationCommentsToBitbucketCloudApi violationCommentsToBitbucketCloudApi() {
    return new ViolationCommentsToBitbucketCloudApi();
  }

  private String bitbucketServerUrl = null;
  private boolean createCommentWithAllSingleFileComments = false;
  private boolean createSingleFileComments = true;
  private String password;
  private String projectKey;
  private int pullRequestId;
  private String repoSlug;
  private String username;
  private List<Violation> violations;
  private boolean commentOnlyChangedContent = false;
  private int commentOnlyChangedContentContext;
  private boolean shouldKeepOldComments;

  private ViolationCommentsToBitbucketCloudApi() {}

  private void checkState() {
    if (username == null || password == null) {
      throw new IllegalStateException(
          "User and Password must be set! They can be set with the API or by setting properties.");
    }
    checkNotNull(bitbucketServerUrl, "BitbucketServerURL");
    checkNotNull(pullRequestId, "PullRequestId");
    checkNotNull(repoSlug, "repoSlug");
    checkNotNull(projectKey, "projectKey");
  }

  public String getBitbucketServerUrl() {
    return bitbucketServerUrl;
  }

  public boolean getCommentOnlyChangedContent() {
    return commentOnlyChangedContent;
  }

  public int getCommentOnlyChangedContentContext() {
    return commentOnlyChangedContentContext;
  }

  public boolean getCreateCommentWithAllSingleFileComments() {
    return createCommentWithAllSingleFileComments;
  }

  public boolean getCreateSingleFileComments() {
    return createSingleFileComments;
  }

  public String getPassword() {
    return password;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public int getPullRequestId() {
    return pullRequestId;
  }

  public String getRepoSlug() {
    return repoSlug;
  }

  public String getUsername() {
    return username;
  }

  public void toPullRequest() throws Exception {
    checkState();
    final CommentsProvider commentsProvider = new CommentsProvider(this);
    createComments(commentsProvider, violations, BITBUCKET_MAX_COMMENT_SIZE);
  }

  public ViolationCommentsToBitbucketCloudApi withBitbucketServerUrl(String bitbucketServerUrl) {
    this.bitbucketServerUrl = bitbucketServerUrl;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCommentOnlyChangedContent(
      boolean commentOnlyChangedContent) {
    this.commentOnlyChangedContent = commentOnlyChangedContent;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCommentOnlyChangedContentContext(
      int commentOnlyChangedContentContext) {
    this.commentOnlyChangedContentContext = commentOnlyChangedContentContext;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCreateCommentWithAllSingleFileComments(
      boolean createCommentWithAllSingleFileComments) {
    this.createCommentWithAllSingleFileComments = createCommentWithAllSingleFileComments;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCreateSingleFileComments(
      boolean createSingleFileComments) {
    this.createSingleFileComments = createSingleFileComments;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withPassword(String password) {
    this.password = password;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withProjectKey(String projectKey) {
    this.projectKey = projectKey;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withPullRequestId(int pullRequestId) {
    this.pullRequestId = pullRequestId;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withRepoSlug(String repoSlug) {
    this.repoSlug = repoSlug;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withUsername(String username) {
    this.username = username;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withViolations(List<Violation> violations) {
    this.violations = violations;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withShouldKeepOldComments(
      boolean shouldKeepOldComments) {
    this.shouldKeepOldComments = shouldKeepOldComments;
    return this;
  }

  public boolean getShouldKeepOldComments() {
    return shouldKeepOldComments;
  }
}
