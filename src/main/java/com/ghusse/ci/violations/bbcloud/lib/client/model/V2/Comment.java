package com.ghusse.ci.violations.bbcloud.lib.client.model.V2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
  @JsonProperty("id")
  private long id;

  @JsonProperty("type")
  private String type;

  @JsonProperty("content")
  private Content content;

  @JsonProperty("inline")
  private CommentPosition position;

  public Comment() {
  }

  public long getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getContent() {
    if (this.content == null) {
      return null;
    }

    return this.content.getRaw();
  }

  public CommentPosition getPosition() {
    return position;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  class Content {
    @JsonProperty("raw")
    private String raw;

    public Content() {
    }

    public String getRaw() {
      return raw;
    }
  }
}
