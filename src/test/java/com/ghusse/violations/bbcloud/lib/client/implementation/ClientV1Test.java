package com.ghusse.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV1;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClient;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v1.Comment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientV1Test {
  @Mock
  private RestClient restClient;

  @InjectMocks
  private ClientV1 target;

  @Test
  public void itShouldPublishALineComment() throws RestClientException, ClientException {
    PullRequestDescription description = new PullRequestDescription("user", "repository", "id");

    this.target.setAuthentication("username", "password");
    this.target.publishLineComment(description, "Content", "File", 42);

    verify(this.restClient, times(1))
            .setAuthentication("username", "password");

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(this.restClient, times(1))
            .post(
                    eq("https://api.bitbucket.org/1.0/repositories/user/repository/pullrequests/id/comments"),
                    commentCaptor.capture()
            );

    Comment captured = commentCaptor.getValue();
    assertEquals("Content", captured.getContent());
    assertEquals(Integer.valueOf(42), captured.getLineTo());
    assertEquals("File", captured.getFile());
  }

  @Test
  public void itShouldPublishAComment() throws RestClientException, ClientException {
    PullRequestDescription description = new PullRequestDescription("user", "repository", "id");

    this.target.publishPullRequestComment(description, "Content");

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(this.restClient, times(1))
            .post(
                    eq("https://api.bitbucket.org/1.0/repositories/user/repository/pullrequests/id/comments"),
                    commentCaptor.capture()
            );

    Comment captured = commentCaptor.getValue();
    assertEquals("Content", captured.getContent());
    assertNull(captured.getFile());
    assertNull(captured.getLineTo());
  }

  @Test
  public void itShouldDeleteAComment() throws RestClientException, ClientException {
    PullRequestDescription description = new PullRequestDescription("user", "repo", "id");

    this.target.deleteComment(description, 42);

    verify(this.restClient, times(1))
            .delete("https://api.bitbucket.org/1.0/repositories/user/repo/pullrequests/id/comments/42");
  }

  @Test
  public void itShouldRethrowAnExceptionWhenDeleting() throws RestClientException {
    PullRequestDescription pullRequest = new PullRequestDescription("user", "repo", "id");

    RestClientException error = mock(RestClientException.class);

    when(this.restClient.delete(anyString()))
            .thenThrow(error);

    try{
      this.target.deleteComment(pullRequest, 42);
      fail("Should have thrown an error");
    }catch (ClientException thrown){
      assertEquals(error, thrown.getCause());
      assertEquals("Unable to delete a comment: 42 Pull request in repo user/repo id: id", thrown.getMessage());
    }
  }

  @Test
  public void itShouldRethrowAnExceptionWhenPublishingAComment() throws RestClientException {
    PullRequestDescription pullRequest = new PullRequestDescription("user", "repo", "id");

    RestClientException error = mock(RestClientException.class);
    Comment comment = mock(Comment.class);

    when(this.restClient.post(anyString(), any()))
            .thenThrow(error);

    try{
      this.target.publishComment(pullRequest, comment);
      fail("Should have thrown an exception");
    }catch (ClientException thrown){
      assertEquals(error, thrown.getCause());
      assertTrue(thrown.getMessage().contains("Unable to publish a comment:"));
      assertTrue(thrown.getMessage().contains("Pull request in repo user/repo id: id"));
    }
  }
}
