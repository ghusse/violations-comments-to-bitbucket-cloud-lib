package com.ghusse.violations.bbcloud.lib;


import com.ghusse.ci.violations.bbcloud.lib.CommentsProvider;
import com.ghusse.ci.violations.bbcloud.lib.CommentsProviderError;
import com.ghusse.ci.violations.bbcloud.lib.DiffParser;
import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.ViolationCommentsToBitbucketCloudApi;
import com.ghusse.ci.violations.bbcloud.lib.client.Client;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment;

import io.reflectoring.diffparser.api.UnifiedDiffParser;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;
import io.reflectoring.diffparser.api.model.Range;
import io.reflectoring.diffparser.api.model.Line;
import io.reflectoring.diffparser.api.model.Line.LineType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.bjurr.violations.comments.lib.model.ChangedFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommentsProviderTest {
  @Mock
  private Client client;

  @Mock
  private DiffParser parser;

  @Mock
  private InputStream inputStream;

  @Mock
  private UnifiedDiffParser uniParser;

  
  @Mock
  private ViolationCommentsToBitbucketCloudApi api;

  @InjectMocks
  private CommentsProvider target;

  private PullRequestDescription description;

  private List<Diff> diffs = new ArrayList<>();
  
  @Before
  public void init() {
    List<Line> lines1 = new ArrayList<>();
    lines1.add(new Line(LineType.NEUTRAL, "line2"));
    lines1.add(new Line(LineType.FROM, "line3"));
    lines1.add(new Line(LineType.TO, "new_line3"));
    lines1.add(new Line(LineType.NEUTRAL, "line4"));
    lines1.add(new Line(LineType.FROM, "line5"));
    lines1.add(new Line(LineType.TO, "new_line5"));
    lines1.add(new Line(LineType.FROM, "line6"));
    lines1.add(new Line(LineType.TO, "new_line6"));
    lines1.add(new Line(LineType.NEUTRAL, "line7"));
    Hunk hunk1 = new Hunk();
    hunk1.setToFileRange(new Range(2,6));
    hunk1.setLines(lines1);

    List<Line> lines2 = new ArrayList<>();
    lines2.add(new Line(LineType.FROM, "line10"));
    lines2.add(new Line(LineType.TO, "new_line10"));
    lines2.add(new Line(LineType.NEUTRAL, "line11"));
    lines2.add(new Line(LineType.FROM, "line12"));
    lines2.add(new Line(LineType.TO, "new_line12"));
    Hunk hunk2 = new Hunk();
    hunk2.setToFileRange(new Range(10,12));
    hunk2.setLines(lines2);

    List<Hunk> hunks = new ArrayList<Hunk>();
    hunks.add(hunk1);
    hunks.add(hunk2);

    Diff diff = new Diff();
    diff.setFromFileName("a/dir/foo");
    diff.setToFileName("b/dir/bar");
    diff.setHunks(hunks);

    diffs.add(diff);
    
    this.description = new PullRequestDescription("repoUser", "repo", "42");
    try {
        when(this.client.getDiff(this.description)).thenReturn(this.inputStream);
    } catch (ClientException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    when(this.uniParser.parse(this.inputStream)).thenReturn(this.diffs);

    this.target.init("user", "pass", this.api, this.description);
    
  }

  @Test
  public void itShouldUseTheClientToPublishAComment() throws ClientException {
    String comment = "comment";

    this.target.createCommentWithAllSingleFileComments(comment);

    verify(this.client, times(1))
            .publishPullRequestComment(this.description, comment);
  }

  @Test
  public void itShouldRethrowAnErrorWhenPublishingAComment() throws ClientException {
    ClientException error = mock(ClientException.class);
    doThrow(error).when(this.client).publishPullRequestComment(this.description, "foo");

    try{
      this.target.createCommentWithAllSingleFileComments("foo");
      fail("Should have thrown an error");
    }catch (CommentsProviderError rethrown){
      assertEquals(error, rethrown.getCause());
      assertEquals("Unable to publish a pull request comment pullRequest: user:repoUser repo:repo id:42", rethrown.getMessage());
      assertEquals(this.description, rethrown.getDescription());
    }
  }

  @Test
  public void itShouldCallTheClientToCreateAFileComment() throws ClientException {
    ChangedFile file = new ChangedFile("foo.txt", new ArrayList<String>());

    this.target.createSingleFileComment(file, 42, "comment");

    verify(this.client, times(1))
            .publishLineComment(this.description, "comment", "foo.txt", 42);
  }

  @Test
  public void itShouldRethrowAnErrorWhenCreatingAFileComment() throws ClientException {
    ClientException error = mock(ClientException.class);
    ChangedFile file = new ChangedFile("foo.txt", new ArrayList<String>());

    doThrow(error).when(this.client).publishLineComment(this.description, "comment", "foo.txt", 42);

    try{
      this.target.createSingleFileComment(file,42, "comment");
      fail("Should have thrown an exception");
    }catch (CommentsProviderError thrown){
      assertEquals(error, thrown.getCause());
      assertEquals(this.description, thrown.getDescription());
      assertEquals("Unable to publish a line comment pullRequest: user:repoUser repo:repo id:42", thrown.getMessage());
    }
  }

  @Test
  public void itShouldGetCommentsAndMapThem() throws ClientException {
    List<Comment> comments = new ArrayList<>();
    Comment comment1 = mock(Comment.class);
    Comment comment2 = mock(Comment.class);
    comments.add(comment1);
    comments.add(comment2);

    when(comment1.getId()).thenReturn(42l);
    when(comment2.getId()).thenReturn(97l);
    when(comment1.getContent()).thenReturn("comment 1");
    when(comment2.getContent()).thenReturn("comment 2");
    when(comment1.getType()).thenReturn("type 1");
    when(comment2.getType()).thenReturn("type 2");

    when(this.client.listCommentsForPullRequest(this.description)).thenReturn(comments);

    List<se.bjurr.violations.comments.lib.model.Comment> result = this.target.getComments();

    assertEquals(2, result.size());

    se.bjurr.violations.comments.lib.model.Comment result1 = result.get(0);
    assertEquals("42", result1.getIdentifier());
    assertEquals("comment 1", result1.getContent());
    assertEquals("type 1", result1.getType());

    assertEquals("97", result.get(1).getIdentifier());
  }

  @Test
  public void itShouldRethrowAnErrorWhenGettingTheComments() throws ClientException {
    ClientException error = mock(ClientException.class);

    when(this.client.listCommentsForPullRequest(this.description))
            .thenThrow(error);

    try{
      this.target.getComments();
      fail("Should have thrown an error");
    }catch (CommentsProviderError thrown){
      assertEquals(error, thrown.getCause());
      assertEquals(this.description, thrown.getDescription());
      assertEquals("Unable to get the list of comments associated to a pull request pullRequest: user:repoUser repo:repo id:42", thrown.getMessage());
    }
  }

  @Test
  public void itShouldReturnTheListOfModifiedFiles() throws ClientException {
    List<String> changed = new ArrayList<>();
    changed.add("foo/bar.md");

    when(this.parser.getChangedFiles(this.inputStream))
            .thenReturn(changed);

    when(this.client.getDiff(this.description))
            .thenReturn(this.inputStream);

    List<ChangedFile> result = this.target.getFiles();

    assertEquals(1, result.size());
    assertEquals("foo/bar.md", result.get(0).getFilename());
    assertNotNull(result.get(0).getSpecifics());
  }

  @Test
  public void itShouldRethrowAnExceptionWhenGettingTheDiff() throws ClientException {
    ClientException error = mock(ClientException.class);

    when(this.client.getDiff(this.description))
            .thenThrow(error);

    try{
      this.target.getFiles();
      fail("Should have thrown an error");
    }catch (CommentsProviderError thrown){
      assertEquals(error, thrown.getCause());
      assertEquals("Unable to get the diff for a pull request pullRequest: user:repoUser repo:repo id:42", thrown.getMessage());
    }
  }

  @Test
  public void itShouldCallTheClientToRemoveEachComment() throws ClientException {
    List<se.bjurr.violations.comments.lib.model.Comment> comments = new ArrayList<>();
    se.bjurr.violations.comments.lib.model.Comment comment1 = mock(se.bjurr.violations.comments.lib.model.Comment.class);
    se.bjurr.violations.comments.lib.model.Comment comment2 = mock(se.bjurr.violations.comments.lib.model.Comment.class);
    when(comment1.getIdentifier()).thenReturn("42");
    when(comment2.getIdentifier()).thenReturn("2001");
    comments.add(comment1);
    comments.add(comment2);

    this.target.removeComments(comments);

    verify(this.client, times(1))
            .deleteComment(this.description, 42l);
    verify(this.client, times(1))
            .deleteComment(this.description, 2001l);

  }

  @Test
  public void itShouldRethrowExceptionWhenRemovingComments() throws ClientException {
    List<se.bjurr.violations.comments.lib.model.Comment> comments = new ArrayList<>();
    se.bjurr.violations.comments.lib.model.Comment comment = mock(se.bjurr.violations.comments.lib.model.Comment.class);

    when(comment.getIdentifier()).thenReturn("42");
    comments.add(comment);

    ClientException error = mock(ClientException.class);

    doThrow(error)
            .when(this.client)
            .deleteComment(this.description, 42);

    try{
      this.target.removeComments(comments);
      fail("Should have throw an error");
    }catch(CommentsProviderError thrown){
      assertEquals(error, thrown.getCause());
      assertTrue(thrown.getMessage(), thrown.getMessage().contains("Unable to delete comments"));
    }
  }

  @Test
  public void itShouldCommentAlways() throws ClientException {
    when(this.api.getCommentOnlyChangedContent()).thenReturn(false);
    ChangedFile changedFile = new ChangedFile("foo", new ArrayList<String>());
    Integer changedLine = 1;

    assertTrue(this.target.shouldComment(changedFile, changedLine));
  }

  @Test
  public void itShouldNotCommentIfDiffIsEmpty() throws ClientException {
    when(this.api.getCommentOnlyChangedContent()).thenReturn(true);
    when(this.api.getCommentOnlyChangedContentContext()).thenReturn(0);
    when(this.client.getDiff(this.description)).thenReturn(this.inputStream);
    when(this.uniParser.parse(this.inputStream)).thenReturn(new ArrayList<Diff>());

    this.target.init("user", "pass", this.api, this.description);

    ChangedFile changedFile = new ChangedFile("dir/foo", new ArrayList<String>());
    Integer changedLine = 1;
    assertFalse(this.target.shouldComment(changedFile, changedLine));
  }

  @Test
  public void itShouldNotCommentOnClientError() throws ClientException {
    ClientException error = mock(ClientException.class);
    when(this.client.getDiff(this.description)).thenThrow(error);
    when(this.api.getCommentOnlyChangedContent()).thenReturn(true);
    when(this.client.getDiff(this.description)).thenReturn(this.inputStream);
    when(this.uniParser.parse(this.inputStream)).thenReturn(this.diffs);

    this.target.init("user", "pass", this.api, this.description);

    ChangedFile changedFile = new ChangedFile("dir/foobar", new ArrayList<String>());
    assertFalse(this.target.shouldComment(changedFile, 1));
  }

  @Test
  public void itShouldNotCommentIfFileNotChanged() throws ClientException {
    when(this.api.getCommentOnlyChangedContent()).thenReturn(true);
    when(this.api.getCommentOnlyChangedContentContext()).thenReturn(0);
    when(this.client.getDiff(this.description)).thenReturn(this.inputStream);
    when(this.uniParser.parse(this.inputStream)).thenReturn(this.diffs);

    this.target.init("user", "pass", this.api, this.description);
    ChangedFile changedFile = new ChangedFile("dir/foobar", new ArrayList<String>());
    Integer changedLine = 3;
    assertFalse(this.target.shouldComment(changedFile, changedLine));
  }

  @Test
  public void itShouldCommentIfFileChangedInLine() throws ClientException {
    when(this.api.getCommentOnlyChangedContent()).thenReturn(true);
    when(this.api.getCommentOnlyChangedContentContext()).thenReturn(0);
    when(this.client.getDiff(this.description)).thenReturn(this.inputStream);
    when(this.uniParser.parse(this.inputStream)).thenReturn(this.diffs);

    this.target.init("user", "pass", this.api, this.description);
    ChangedFile changedFile = new ChangedFile("dir/bar", new ArrayList<String>());
    Integer changedLine = 3;
    assertTrue(this.target.shouldComment(changedFile, changedLine));
  }

  @Test
  public void itShouldCommentIfFileChangedInDifferentLine() throws ClientException {
    when(this.api.getCommentOnlyChangedContent()).thenReturn(true);
    when(this.api.getCommentOnlyChangedContentContext()).thenReturn(0);
    when(this.client.getDiff(this.description)).thenReturn(this.inputStream);
    when(this.uniParser.parse(this.inputStream)).thenReturn(this.diffs);

    this.target.init("user", "pass", this.api, this.description);
    ChangedFile changedFile = new ChangedFile("dir/bar", new ArrayList<String>());
    Integer changedLine = 4;
    assertFalse(this.target.shouldComment(changedFile, changedLine));
  }
}