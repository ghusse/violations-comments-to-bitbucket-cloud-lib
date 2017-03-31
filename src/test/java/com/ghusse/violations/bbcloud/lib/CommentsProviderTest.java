package com.ghusse.violations.bbcloud.lib;


import com.ghusse.ci.violations.bbcloud.lib.CommentsProvider;
import com.ghusse.ci.violations.bbcloud.lib.CommentsProviderError;
import com.ghusse.ci.violations.bbcloud.lib.DiffParser;
import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.Client;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment;
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

  @InjectMocks
  private CommentsProvider target;

  private PullRequestDescription description;

  @Before
  public void init(){
    this.description = new PullRequestDescription("repoUser", "repo", "42");
    this.target.init("user", "pass", this.description);
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
}
