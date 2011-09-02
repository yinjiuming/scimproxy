package info.simplecloud.scimproxy;

import info.simplecloud.core.User;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

public class ScimUserServletPutTest {

    private static HttpTester     request  = new HttpTester();
    private static HttpTester     response = new HttpTester();
    private static ServletTester  tester   = null;

    private static String id       = "";
    private static String etag       = "";

    @BeforeClass
    public static void setUp() throws Exception {
        tester = new ServletTester();
        tester.addServlet(ScimUserServlet.class, "/v1/User/*");
        tester.addServlet(DefaultServlet.class, "/");
        tester.start();

        User scimUser = new User("ABC123-put");
        scimUser.setUserName("Alice");

        request.setMethod("POST");
        request.setVersion("HTTP/1.0");
        request.setHeader("Authorization", "Basic dXNyOnB3");

        request.setURI("/v1/User");
        request.setHeader("Content-Length", Integer.toString(scimUser.getUser("JSON").length()));
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setContent(scimUser.getUser("JSON"));
        response.parse(tester.getResponses(request.generate()));

        User tmp = new User(response.getContent(), "JSON");
        id = tmp.getId();
        etag = tmp.getMeta().getVersion();
    }

    @Test
    public void putUser() throws Exception {
        User scimUser = new User("ABC123-put");
        scimUser.setId(id);
        scimUser.setUserName("Bob");

        request.setMethod("PUT");
        request.setVersion("HTTP/1.0");

        request.setURI("/v1/User/" + id);

        request.setHeader("Content-Length", Integer.toString(scimUser.getUser("JSON").length()));
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setContent(scimUser.getUser("JSON"));
        request.setHeader("ETag", etag);

        response.parse(tester.getResponses(request.generate()));

        Assert.assertEquals(200, response.getStatus());

        User returnedUser = new User(response.getContent(), "JSON");

        Assert.assertEquals(id, returnedUser.getId());
        Assert.assertEquals("Bob", returnedUser.getUserName());
    }

    @Test
    public void putInvalidUser() throws Exception {
        request.setMethod("PUT");
        request.setVersion("HTTP/1.0");

        request.setURI("/v1/User/" + id);

        request.setHeader("Content-Length", Integer.toString("very invalid user".length()));
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setContent("very invalid user");
        response.parse(tester.getResponses(request.generate()));

        Assert.assertEquals(400, response.getStatus());

    }

}
