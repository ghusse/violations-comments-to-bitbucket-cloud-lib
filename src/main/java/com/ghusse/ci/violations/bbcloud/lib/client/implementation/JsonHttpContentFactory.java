package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

public class JsonHttpContentFactory {
  private ObjectMapper mapper;

  @Inject
  public JsonHttpContentFactory(ObjectMapper mapper){
    this.mapper = mapper;
  }

  public JsonHttpContent create(Object value){
    return new JsonHttpContent(mapper, value);
  }
}
