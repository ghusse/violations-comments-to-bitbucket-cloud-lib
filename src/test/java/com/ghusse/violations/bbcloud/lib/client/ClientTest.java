package com.ghusse.violations.bbcloud.lib.client;

import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.Client;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV1;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV2;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.model.V2.Comment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientTest {
  @Mock
  private ClientV2 clientV2;

  @Mock
  private ClientV1 clientV1;

  @Mock
  private PullRequestDescription pullRequestDescription;

  @InjectMocks
  private Client target = new Client();

  @Test
  public void listCommentsForPullRequestShouldProxyClient2() throws IOException, RestClientException, ClientException {
    List<Comment> expectedResult = new ArrayList<Comment>();

    when(this.clientV2.listCommentsForPullRequest(this.pullRequestDescription))
            .thenReturn(expectedResult);

    List<Comment> result = this.target.listCommentsForPullRequest(this.pullRequestDescription);

    assertEquals(expectedResult, result);
  }

  @Test
  public void itShouldSetTheAuthenticationOnAllImplementations() throws IOException {
    String userName = "user";
    String password = "pass";

    this.target.setAuthentication(userName, password);

    verify(this.clientV1, times(1))
            .setAuthentication(userName, password);

    verify(this.clientV2, times(1))
            .setAuthentication(userName, password);
  }
}
