package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.model.V2.Comment;
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

  @Inject
  private RestClient client;

  @Inject
  private ObjectMapper mapper;

  private String userName;
  private String password;

  public ClientV2() {
  }

  public void setAuthentication(String userName, String password) {
    this.client.setAuthentication(userName, password);
  }

  public List<Comment> listCommentsForPullRequest(PullRequestDescription description) throws ClientException {
    return listCommentsForPullRequestFromPage(description, 1);
  }

  private List<Comment> listCommentsForPullRequestFromPage(PullRequestDescription description, int page) throws ClientException {
    String url = String.format(Locale.ENGLISH,
            "%s/repositories/%s/%s/pullrequests/%s/comments?page=%d",
            ENDPOINT,
            description.getUserName(),
            description.getRepositorySlug(),
            description.getId(),
            page);

    InputStream content = null;
    try {
      content = this.client.get(url);
    } catch (RestClientException e) {
      throw new ClientException("Error while requesting the api.", description, e);
    }

    TypeReference<PaginatedResponse<Comment>> type = new TypeReference<PaginatedResponse<Comment>>() {};
    PaginatedResponse<Comment> pageComments = null;
    try {
      pageComments = this.mapper.readValue(content, type);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (pageComments.getPageLength() > page) {
      List<Comment> nextComments = listCommentsForPullRequestFromPage(description, page + 1);

      ArrayList<Comment> result = new ArrayList<Comment>(pageComments.getValues().size() + nextComments.size());
      result.addAll(pageComments.getValues());
      result.addAll(nextComments);

      return result;
    }

    return pageComments.getValues();
  }
}
