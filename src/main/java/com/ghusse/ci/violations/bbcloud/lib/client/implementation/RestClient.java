package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ghusse on 19/03/2017.
 */
public class RestClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

  private String userName;
  private String password;

  @Inject
  private NetHttpTransport transport;

  @Inject
  private JsonHttpContentFactory contentFactory;

  private HttpRequestFactory requestFactory;

  public RestClient() {
  }

  public InputStream get(String url) throws IOException {
    HttpRequestFactory factory = this.getRequestFactory();

    HttpRequest request = factory.buildGetRequest(new GenericUrl(url));

    return this.sendRequest(request);
  }

  public InputStream post(String url, Object data) throws IOException {
    HttpRequestFactory factory = this.getRequestFactory();

    JsonHttpContent content = contentFactory.create(data);

    HttpRequest request = factory.buildPostRequest(new GenericUrl(url), content);

    return this.sendRequest(request);
  }

  public void delete(String url) throws IOException {
    HttpRequestFactory factory = this.getRequestFactory();

    HttpRequest request = factory.buildDeleteRequest(new GenericUrl(url));

    this.sendRequest(request);
  }

  private InputStream sendRequest(HttpRequest request) throws IOException {
    this.authenticate(request);

    HttpResponse response = request.execute();

    int statusCode = response.getStatusCode();

    if (statusCode < 200 || statusCode >= 300) {
      LOGGER.warn("HTTP error on the Bitbucket API v2. Status: {}, Body: {}", statusCode, response.parseAsString());
      throw new RuntimeException("HTTP error on the Bitbucket API v2");
    }

    return response.getContent();
  }

  private void authenticate(HttpRequest request) {
    if (this.userName != null && this.password != null) {
      HttpHeaders headers = new HttpHeaders();
      headers.setBasicAuthentication(this.userName, this.password);

      request.setHeaders(headers);
    }
  }

  private HttpRequestFactory getRequestFactory() {
    if (this.requestFactory == null) {
      this.requestFactory = this.transport.createRequestFactory();
    }

    return this.requestFactory;
  }

  public void setAuthentication(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }
}
