package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.google.api.client.http.HttpHeaders;

import java.util.List;

/**
 * Created by Ghusse on 31/03/2017.
 */
public class AuthenticatedRedirectHttpHeaders extends HttpHeaders {
  public AuthenticatedRedirectHttpHeaders(){
    super();
  }

  /**
   *
   * @param authorization
   * @return
   */
  @Override
  public HttpHeaders setAuthorization(List<String> authorization) {
    if (authorization != null && !authorization.isEmpty()){
      super.setAuthorization(authorization);
    }

    return this;
  }
}
