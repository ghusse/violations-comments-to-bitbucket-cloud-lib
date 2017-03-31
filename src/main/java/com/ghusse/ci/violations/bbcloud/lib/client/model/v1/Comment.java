package com.ghusse.ci.violations.bbcloud.lib.client.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
public class Comment {
  @JsonProperty("comment_id")
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private long id;

  @JsonProperty("content")
  private String content;

  @JsonProperty("filename")
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private String file;

  @JsonProperty("line_from")
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private Integer lineFrom;

  @JsonProperty("line_to")
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private Integer lineTo;

  public Comment() {
    // This class is created with Jackson, from JSON
    // It needs a default constructor
  }

  public Comment(String content, String fileName, int lineNumber) {
    this.content = content;
    this.file = fileName;
    this.lineTo = lineNumber;
  }

  public Comment(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public Integer getLineFrom() {
    return lineFrom;
  }

  public Integer getLineTo() {
    return lineTo;
  }

  public String getFile() {
    return file;
  }

  @Override
  public String toString(){
    return String.format(Locale.ENGLISH,
            "%s id: %d, file: %s, line: %d-%d, content: %s",
            super.toString(),
            this.id,
            this.file,
            this.lineFrom,
            this.lineTo,
            this.content);
  }
}
