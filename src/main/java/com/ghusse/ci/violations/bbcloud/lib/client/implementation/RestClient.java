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

  private NetHttpTransport transport;

  private JsonHttpContentFactory contentFactory;

  private HttpRequestFactory requestFactory;

  @Inject
  public RestClient(NetHttpTransport transport, JsonHttpContentFactory contentFactory) {
    this.contentFactory = contentFactory;
    this.transport = transport;

    this.requestFactory = this.transport.createRequestFactory();
  }

  public InputStream get(String url) throws RestClientException {
    HttpRequest request;
    GenericUrl genericUrl = new GenericUrl(url);

    try {
      request = this.requestFactory.buildGetRequest(genericUrl);
    } catch (IOException e) {
      throw new RestClientException("Unable to create a get request", "GET", genericUrl, e);
    }

    return this.sendRequest(request);
  }

  public InputStream post(String url, Object data) throws RestClientException {
    JsonHttpContent content = contentFactory.create(data);
    GenericUrl genericUrl = new GenericUrl(url);
    HttpRequest request;

    try {
      request = this.requestFactory.buildPostRequest(genericUrl, content);
    } catch (IOException e) {
      throw new RestClientException("Unable to create a post request", "POST", genericUrl, e);
    }

    return this.sendRequest(request);
  }

  public InputStream delete(String url) throws RestClientException {
    HttpRequest request;
    GenericUrl genericUrl = new GenericUrl(url);

    try {
      request = this.requestFactory.buildDeleteRequest(genericUrl);
    } catch (IOException e) {
      throw new RestClientException("Unable to create a delete request", "DELETE", genericUrl, e);
    }

    return this.sendRequest(request);
  }

  private InputStream sendRequest(HttpRequest request) throws RestClientException {
    this.authenticate(request);

    HttpResponse response;
    try {
      response = request.execute();
    } catch (IOException e) {
      throw new RestClientException(
              "Unable to send the request to the API",
              request.getRequestMethod(),
              request.getUrl(),
              e);
    }

    int statusCode = response.getStatusCode();

    if (statusCode < 200 || statusCode >= 300) {
      String responseContent = null;

      try{
        responseContent = response.parseAsString();
      }catch (IOException parseException){
        LOGGER.warn("Unable to parse the response's content. Verb: {}. Url: {}. Status code: {}",
                request.getRequestMethod(),
                request.getUrl(),
                statusCode);
        LOGGER.warn("Parsing error", parseException);
      }

      throw new RestClientException(
              "Received an error code from the API.",
              request.getRequestMethod(),
              request.getUrl(),
              statusCode,
              responseContent);
    }

    try {
      return response.getContent();
    } catch (IOException e) {
      throw new RestClientException(
              "Unable to get the response's content.",
              request.getRequestMethod(),
              request.getUrl(),
              statusCode,
              e);
    }
  }

  private void authenticate(HttpRequest request) {
    if (this.userName != null && this.password != null) {
      // By default, when following a redirection, the HttpRequest resets
      // some headers, including the authentication header.
      // Bitbucket uses redirection for some GET requests, and we need
      // the redirected query to be authenticated as well.
      // As the HttpRequest is final, this seems to be the best solution
      HttpHeaders headers = new AuthenticatedRedirectHttpHeaders();

      headers.setBasicAuthentication(this.userName, this.password);
      headers.setUserAgent(RestClient.class.getCanonicalName());

      request.setHeaders(headers);
    }
  }

  public void setAuthentication(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }
}
