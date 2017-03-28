package com.ghusse.ci.violations.bbcloud.lib.client.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentPosition {
  @JsonProperty("path")
  private String path;

  @JsonProperty("from")
  private Integer from;

  @JsonProperty("to")
  private Integer to;

  public CommentPosition() {
    // This class is created with Jackson, from JSON
    // It needs a default constructor
  }

  public Integer getFrom() {
    return from;
  }

  public Integer getTo() {
    return to;
  }

  public String getPath() {
    return path;
  }
}
