package com.ghusse.ci.violations.bbcloud.lib.client.model.V2;

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
