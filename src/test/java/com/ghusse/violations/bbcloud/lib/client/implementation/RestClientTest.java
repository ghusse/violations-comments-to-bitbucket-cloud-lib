package com.ghusse.violations.bbcloud.lib.client.implementation;

import com.ghusse.ci.violations.bbcloud.lib.client.implementation.JsonHttpContent;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.JsonHttpContentFactory;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClient;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClientException;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {
  private static final String USERNAME = "user";
  private static final String PASSWORD = "pass";
  private static final String URL = "http://nowhere";

  @Mock
  private NetHttpTransport transport;

  private HttpRequestFactory requestFactory;

  @Mock
  private HttpRequest request;

  @Mock
  private HttpResponse response;

  @Mock
  private InputStream content;

  @Mock
  private JsonHttpContentFactory contentFactory;

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
    when(this.requestFactory.buildPostRequest(any(GenericUrl.class), any(HttpContent.class)))
            .thenReturn(this.request);
    when(this.requestFactory.buildDeleteRequest(any(GenericUrl.class)))
            .thenReturn(this.request);

    when(this.request.execute())
            .thenReturn(this.response);

    when(this.response.getContent())
            .thenReturn(this.content);

    this.target = new RestClient(this.transport, this.contentFactory);
  }

  @Test
  public void itShouldExecuteAnAuthenticatedHttpCall() throws IOException, RestClientException {
    when(this.response.getStatusCode())
            .thenReturn(200);

    target.setAuthentication(USERNAME, PASSWORD);
    InputStream result = target.get(URL);

    assertEquals(this.content, result);

    ArgumentCaptor<GenericUrl> urlArgument = ArgumentCaptor.forClass(GenericUrl.class);
    verify(requestFactory, times(1))
            .buildGetRequest(urlArgument.capture());
    assertEquals(URL, urlArgument.getValue().toString());

    assertAuthentication();
  }

  @Test
  public void itShouldNotSetTheHeadersIfAuthenticationIsNotSet() throws IOException, RestClientException {
    when(this.response.getStatusCode())
            .thenReturn(200);

    target.get(URL);
    verify(this.request, never()).setHeaders(any(HttpHeaders.class));

    target.setAuthentication(null, PASSWORD);
    target.get(URL);
    verify(this.request, never()).setHeaders(any(HttpHeaders.class));

    target.setAuthentication(USERNAME, null);
    target.get(URL);
    verify(this.request, never()).setHeaders(any(HttpHeaders.class));
  }

  private void assertAuthentication() {
    ArgumentCaptor<HttpHeaders> headersArgument = ArgumentCaptor.forClass(HttpHeaders.class);
    verify(this.request, times(1))
            .setHeaders(headersArgument.capture());

    assertEquals("Basic dXNlcjpwYXNz", headersArgument.getValue().getAuthorization());
  }

  @Test
  public void itShouldSendAnAuthenticatedPostRequest() throws IOException, RestClientException {
    Object data = new Object();

    JsonHttpContent jsonContent = mock(JsonHttpContent.class);

    when(this.contentFactory.create(data))
            .thenReturn(jsonContent);

    when(this.response.getStatusCode())
            .thenReturn(200);

    this.target.setAuthentication(USERNAME, PASSWORD);
    InputStream result = this.target.post(URL, data);

    assertEquals(this.content, result);

    ArgumentCaptor<GenericUrl> urlArgument = ArgumentCaptor.forClass(GenericUrl.class);
    ArgumentCaptor<HttpContent> contentArgument = ArgumentCaptor.forClass(HttpContent.class);
    verify(requestFactory, times(1))
            .buildPostRequest(urlArgument.capture(), contentArgument.capture());
    assertEquals(URL, urlArgument.getValue().toString());
    assertEquals(jsonContent, contentArgument.getValue());

    verify(this.contentFactory, times(1)).create(data);
  }

  @Test
  public void itShouldSendAnAuthenticatedDeleteHttpRequest() throws IOException, RestClientException {
    when(this.response.getStatusCode())
            .thenReturn(200);

    this.target.setAuthentication(USERNAME, PASSWORD);
    InputStream result = this.target.delete(URL);

    ArgumentCaptor<GenericUrl> urlArgument = ArgumentCaptor.forClass(GenericUrl.class);
    verify(requestFactory, times(1))
            .buildDeleteRequest(urlArgument.capture());
    assertEquals(URL, urlArgument.getValue().toString());

    assertAuthentication();

    assertEquals(this.content, result);
  }

  @Test
  public void itShouldThrowAnErrorIfHttpCodeIs199() throws IOException {
    assertErrorWithHttpCode(199);
  }

  @Test
  public void itShouldThrowAnErrorIfHttpCodeIs300() throws IOException {
    assertErrorWithHttpCode(300);
  }

  private void assertErrorWithHttpCode(int httpCode) throws IOException {
    when(this.response.getStatusCode())
            .thenReturn(httpCode);

    when(this.request.getRequestMethod()).thenReturn("VERB");
    when(this.request.getUrl()).thenReturn(new GenericUrl(URL));
    when(this.response.parseAsString()).thenReturn("RESPONSE");

    try {
      this.target.delete(URL);
      Assert.fail("Should have thrown an exception");
    }catch (RestClientException e){
      assertEquals(
              String.format(Locale.ENGLISH, "Received an error code from the API. VERB %s. Received code %d: RESPONSE", URL, httpCode),
              e.getMessage());
    }
  }
}
