package com.ghusse.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClient;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {
  @Mock
  private NetHttpTransport transport;

  private HttpRequestFactory requestFactory;

  @Mock
  private HttpRequest request;

  @Mock
  private HttpResponse response;

  @Mock
  private InputStream content;

  @InjectMocks
  private RestClient target;

  @Before
  public void setup() throws IOException {
    // Cannot use @Mock annotation
    // Otherwise this mock gets injected in the target
    this.requestFactory = mock(HttpRequestFactory.class);

    when(this.transport.createRequestFactory())
            .thenReturn(this.requestFactory);

    when(this.requestFactory.buildGetRequest(any(GenericUrl.class)))
            .thenReturn(this.request);

    when(this.request.execute())
            .thenReturn(this.response);

    when(this.response.getContent())
            .thenReturn(this.content);
  }

  @Test
  public void itShouldExecuteAnAuthenticatedHttpCallAndParseTheResult() throws IOException {
    String userName = "user";
    String password = "pass";
    String url = "http://nowhere";

    TestClass expectedResult = mock(TestClass.class);

    when(this.response.getStatusCode())
            .thenReturn(200);

    target.setAuthentication(userName, password);
    InputStream result = target.get(url);

    assertEquals(this.content, result);

    ArgumentCaptor<GenericUrl> urlArgument = ArgumentCaptor.forClass(GenericUrl.class);
    verify(requestFactory, times(1))
            .buildGetRequest(urlArgument.capture());
    assertEquals("http://nowhere", urlArgument.getValue().toString());

    ArgumentCaptor<HttpHeaders> headersArgument = ArgumentCaptor.forClass(HttpHeaders.class);
    verify(this.request, times(1))
            .setHeaders(headersArgument.capture());

    assertEquals("Basic dXNlcjpwYXNz", headersArgument.getValue().getAuthorization());
  }

  /**
   * Used to test response deserialization
   */
  class TestClass {
    public TestClass() {
    }
  }
}
