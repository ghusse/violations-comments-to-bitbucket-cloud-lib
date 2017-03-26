package com.ghusse.violations.bbcloud.lib.client.implementation;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV2;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClient;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.model.V2.Comment;
import com.ghusse.ci.violations.bbcloud.lib.client.model.PaginatedResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientV2Test {
  @Mock
  private RestClient restClient;

  @Mock
  private InputStream content;

  @Mock
  private ObjectMapper mapper;

  @InjectMocks
  private ClientV2 target;

  @Test
  public void itShouldSetTheClientAuthentication() {
    String user = "login";
    String password = "pass";

    this.target.setAuthentication(user, password);

    verify(this.restClient, times(1))
            .setAuthentication(user, password);
  }

  @Test
  public void itShouldListComments() throws IOException, RestClientException {
    PaginatedResponse<Comment> response = mock(PaginatedResponse.class);

    when(this.mapper.readValue(any(InputStream.class), any(TypeReference.class)))
            .thenReturn(response);

    List<Comment> pageResult = new ArrayList<Comment>();
    when(response.getValues()).thenReturn(pageResult);

    PullRequestDescription description = new PullRequestDescription("user", "repo", "pr");

    when(this.restClient.get(anyString()))
            .thenReturn(this.content);

    List<Comment> result = this.target.listCommentsForPullRequest(description);

    assertEquals(pageResult, result);

    verify(this.restClient, times(1))
            .get("https://api.bitbucket.org/2.0/repositories/user/repo/pullrequests/pr/comments?page=1");
  }

  @Test
  public void itShouldCombineMultiplePagesResults() throws IOException, RestClientException {
    Comment firstComment = mock(Comment.class);
    Comment secondComment = mock(Comment.class);

    List<Comment> firstPageResults = new ArrayList<Comment>();
    firstPageResults.add(firstComment);

    List<Comment> secondPageResults = new ArrayList<Comment>();
    secondPageResults.add(secondComment);

    PaginatedResponse<Comment> firstResponse = mock(PaginatedResponse.class);
    when(firstResponse.getPageLength()).thenReturn(2);
    when(firstResponse.getValues()).thenReturn(firstPageResults);

    PaginatedResponse<Comment> secondResponse = mock(PaginatedResponse.class);
    when(secondResponse.getPageLength()).thenReturn(2);
    when(secondResponse.getValues()).thenReturn(secondPageResults);

    when(this.restClient.get(anyString()))
            .thenReturn(this.content);

    when(this.mapper.readValue(any(InputStream.class), any(TypeReference.class)))
            .thenReturn(firstResponse)
            .thenReturn(secondResponse);

    PullRequestDescription description = new PullRequestDescription("user", "repo", "theId");

    List<Comment> result = this.target.listCommentsForPullRequest(description);

    assertEquals(2, result.size());
    assertEquals(firstComment, result.get(0));
    assertEquals(secondComment, result.get(1));

    verify(this.restClient, times(1))
            .get("https://api.bitbucket.org/2.0/repositories/user/repo/pullrequests/theId/comments?page=1");

    verify(this.restClient, times(1))
            .get("https://api.bitbucket.org/2.0/repositories/user/repo/pullrequests/theId/comments?page=2");
  }
}
