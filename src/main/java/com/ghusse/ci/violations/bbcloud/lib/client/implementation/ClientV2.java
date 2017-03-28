package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment;
import com.ghusse.ci.violations.bbcloud.lib.client.model.PaginatedResponse;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client specific to the v2 API
 */
public class ClientV2 {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientV2.class);

  private static final String ENDPOINT = "https://api.bitbucket.org/2.0";

  private RestClient client;
  private ObjectMapper mapper;
  
  @Inject
  public ClientV2(RestClient client, ObjectMapper mapper) {
    this.client = client;
    this.mapper = mapper;
  }

  public void setAuthentication(String userName, String password) {
    this.client.setAuthentication(userName, password);
  }

  public List<Comment> listCommentsForPullRequest(PullRequestDescription description) throws ClientException {
    return listCommentsForPullRequestFromPage(description, 1);
  }

  private List<Comment> listCommentsForPullRequestFromPage(PullRequestDescription description, int page) throws ClientException {
    LOGGER.debug("Lists comments for page {}", page);

    String url = String.format(Locale.ENGLISH,
            "%s/repositories/%s/%s/pullrequests/%s/comments?page=%d",
            ENDPOINT,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId(),
            page);

    InputStream content;
    try {
      content = this.client.get(url);
    } catch (RestClientException e) {
      throw new ClientException("Error while requesting the api.", description, e);
    }

    TypeReference<PaginatedResponse<Comment>> type = new TypeReference<PaginatedResponse<Comment>>() {};
    PaginatedResponse<Comment> pageComments;
    try {
      pageComments = this.mapper.readValue(content, type);
    } catch (IOException e) {
      throw new ClientException("Unable to parse the response.", description, content, e);
    }

    LOGGER.debug("Received {} comments", pageComments.getSize());

    if (pageComments.getPageLength() > page) {
      List<Comment> nextComments = listCommentsForPullRequestFromPage(description, page + 1);

      ArrayList<Comment> result = new ArrayList<>(pageComments.getValues().size() + nextComments.size());
      result.addAll(pageComments.getValues());
      result.addAll(nextComments);

      return result;
    }

    return pageComments.getValues();
  }
}
