package com.ghusse.violations.bbcloud.lib.client;

import com.ghusse.ci.violations.bbcloud.lib.CommentsProvider;
import com.ghusse.ci.violations.bbcloud.lib.PullRequestDescription;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.ClientV2;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.RestClientException;
import com.ghusse.ci.violations.bbcloud.lib.client.model.v2.CommentPosition;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Ghusse on 19/03/2017.
 */
public class CommentsProviderIT {
    private static final String CONFIG_FILE_NAME = "config.properties";

    private CommentsProvider commentsProvider;
    private ClientV2 clientV2;

    private static Properties config = new Properties();

    @BeforeClass
    public static void prepare() throws IOException {
        readConfig();
    }

    @Before
    public void setup(){
        Injector injector = Guice.createInjector();

        this.commentsProvider = injector.getInstance(CommentsProvider.class);
        this.clientV2 = injector.getInstance(ClientV2.class);
    }

    @Test
    public void listCommentsFromSimplePullRequest(){
        this.initOnPullRequest(1);

        List<Comment> result = this.commentsProvider.getComments();

        assertEquals(2, result.size());

        Comment firstComment = result.get(0);
        assertEquals("33419180", firstComment.getIdentifier());
        assertEquals("Pull request comment", firstComment.getContent());
        assertEquals("pullrequest_comment", firstComment.getType());

        Comment secondComment = result.get(1);
        assertEquals("33419181", secondComment.getIdentifier());
        assertEquals("File comment", secondComment.getContent());
        assertEquals("pullrequest_comment", secondComment.getType());
    }

    @Test
    public void itShouldCreateAPullRequestComment(){
        this.initOnPullRequest(2);

        List<Comment> commentsBefore = this.commentsProvider.getComments();

        String content = getRandomContent();

        this.commentsProvider.createCommentWithAllSingleFileComments(content);

        List<Comment> commentsAfter = this.commentsProvider.getComments();

        assertEquals(commentsBefore.size() + 1, commentsAfter.size());
        assertEquals(content, commentsAfter.get(commentsAfter.size() -1).getContent());

        this.commentsProvider.removeComments(commentsAfter);
    }

    @Test
    public void itShouldDeleteAPullRequestComment(){
        this.initOnPullRequest(2);

        String content = getRandomContent();
        this.commentsProvider.createCommentWithAllSingleFileComments(content);

        List<Comment> list = this.commentsProvider.getComments();

        Comment toBeDeleted = findComment(list, content);
        List<Comment> deletionList = new ArrayList<>();
        deletionList.add(toBeDeleted);

        this.commentsProvider.removeComments(deletionList);

        List<Comment> afterDeletion = this.commentsProvider.getComments();

        assertEquals(list.size() - 1, afterDeletion.size());
        assertNull(findComment(afterDeletion, content));
    }

    @Test
    public void itShouldCreateASingleFileComment() throws IOException, RestClientException, ClientException {
        PullRequestDescription description = this.initOnPullRequest(2);

        List<Comment> commentsBefore = this.commentsProvider.getComments();

        ChangedFile file = new ChangedFile("README.md", new ArrayList<String>());
        String content = getRandomContent();

        this.commentsProvider.createSingleFileComment(file, 3, content);

        List<com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment> commentsAfter = this.clientV2.listCommentsForPullRequest(description);

        assertEquals(commentsBefore.size() + 1, commentsAfter.size());

        com.ghusse.ci.violations.bbcloud.lib.client.model.v2.Comment last = commentsAfter.get(commentsAfter.size() - 1);
        assertEquals(content, last.getContent());

        CommentPosition position = last.getPosition();
        assertNotNull(position);
        assertNull(position.getFrom());
        assertEquals(3, position.getTo().intValue());
        assertEquals("README.md", position.getPath());

        this.commentsProvider.removeComments(this.commentsProvider.getComments());
    }

    @Test
    public void itShouldListAllModifiedFilesByAPullRequest(){
        this.initOnPullRequest(3);

        List<ChangedFile> changes = this.commentsProvider.getFiles();

        assertEquals(2, changes.size());
        assertEquals("README_renamed.md", changes.get(0).getFilename());
        assertEquals("added.md", changes.get(1).getFilename());
    }

    private PullRequestDescription initOnPullRequest(long id){
        PullRequestDescription description = new PullRequestDescription("ghusse", "test", Long.toString(id));

        String userName = config.getProperty("username");
        String pass = config.getProperty("password");
        this.commentsProvider.init(userName, pass, description);
        this.clientV2.setAuthentication(userName, pass);

        return description;
    }

    private Comment findComment(List<Comment> list, String content){
        for (Comment comment : list) {
            if (content.equals(comment.getContent())){
                return comment;
            }
        }

        return null;
    }

    private static String getRandomContent() {
        return String.format(Locale.ENGLISH, "This is some random content %d", Math.round(Math.random() * 10000));
    }

    private static void readConfig() throws IOException {
        InputStream input = null;

        try {
            input = CommentsProviderIT.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);

            if(input==null){
                throw new IOException("Unable to find the file");
            }

            //load a properties file from class path, inside static method
            config.load(input);
        } finally{
            if(input!=null){
                input.close();
            }
        }
    }
}
