package com.ghusse.ci.violations.bbcloud.lib.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedResponse<T> {
  @JsonProperty("pagelen")
  private int pageLength;

  @JsonProperty("size")
  private int size;

  @JsonProperty("values")
  private List<T> values;

  @JsonProperty("page")
  private int page;

  @JsonProperty("next")
  private String next;

  public PaginatedResponse() {
    // This class is created with Jackson, from JSON
    // It needs a default constructor
  }

  public int getPageLength() {
    return pageLength;
  }

  public int getSize() {
    return size;
  }

  public List<T> getValues() {
    return new ArrayList<>(values);
  }

  public int getPage() {
    return page;
  }

  public String getNext() {
    return next;
  }
}
