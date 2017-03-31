package com.ghusse.violations.bbcloud.lib.client.model.v1;

import com.ghusse.ci.violations.bbcloud.lib.client.model.v1.Comment;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CommentTest {
  @Test
  public void itShouldReturnPropertiesInToString(){
    Comment comment = new Comment("Comment", "file", 42);

    String result = comment.toString();

    assertTrue("Should contain properties values", result.contains("id: 0, file: file, line: null-42, content: Comment"));
    assertTrue("Should contain the class name", result.contains(Comment.class.getName()));
  }
}
