package com.ghusse.ci.violations.bbcloud.lib.client.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.HttpMediaType;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ghusse on 23/03/2017.
 */
public class JsonHttpContent extends AbstractHttpContent {
  private ObjectMapper mapper;

  private Object value;

  protected JsonHttpContent(ObjectMapper mapper, Object value){
    super(new HttpMediaType("application", "json"));
    this.mapper = mapper;
    this.value = value;
  }

  @Override
  public void writeTo(OutputStream outputStream) throws IOException {
    if (this.value != null){
      this.mapper.writeValue(outputStream, this.value);
    }
  }
}
