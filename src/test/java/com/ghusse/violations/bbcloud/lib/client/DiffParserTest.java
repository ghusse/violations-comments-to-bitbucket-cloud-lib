package com.ghusse.violations.bbcloud.lib.client;

import com.ghusse.ci.violations.bbcloud.lib.DiffParser;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DiffParserTest {
  private DiffParser target;

  @Before
  public void setup(){
    this.target = new DiffParser();
  }

  @Test
  public void itShouldListAllAddedFilesInADiff(){
    InputStream bigDiff = loadDiff("bigdiff");

    List<String> files = this.target.getChangedFiles(bigDiff);

    assertEquals(9, files.size());
  }

  @Test
  public void itShouldOnlyCountValidMetaLines(){
    String fake = "diff --git files\n" +
            "--- a/foo/bar\n" +
            "+++ b/foo/bar\n" +
            "@@ 0,0 1,1@@banana\n" +
            "+++ I'm gonna hack you\n" +
            "diff --git other files\n" +
            "--- a/foo/world\n" +
            "+++ /dev/null\n";

    InputStream stream = new ByteArrayInputStream(fake.getBytes(StandardCharsets.UTF_8));

    List<String> files = this.target.getChangedFiles(stream);

    assertEquals(1, files.size());

    assertEquals("foo/bar", files.get(0));
  }

  private InputStream loadDiff(String name){
    return Thread.currentThread().getContextClassLoader().getResourceAsStream("diff/" + name + ".diff");
  }
}
