package api;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import spark.SparkBase;
import spark.route.SimpleRouteMatcher;
import spark.webserver.MatcherFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock server for Spark Jetty.
 *
 * Created by simone on 14/02/16.
 */
public class MockServer {

    private class DelegatingServletInputStream extends ServletInputStream {

        private final InputStream sourceStream;


        /**
         * Create a DelegatingServletInputStream for the given source stream.
         * @param sourceStream the source stream (never <code>null</code>)
         */
        public DelegatingServletInputStream(InputStream sourceStream) {
            this.sourceStream = sourceStream;
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return this.sourceStream;
        }


        public int read() throws IOException {
            return this.sourceStream.read();
        }

        public void close() throws IOException {
            super.close();
            this.sourceStream.close();
        }
    }

    /**
     * Support class to access the static data of SparkBase.
     */
    class SparkBaseMock extends SparkBase {
        public SimpleRouteMatcher getRoute() {
            return super.routeMatcher;
        }
        public void clearRoute() {
            if (super.routeMatcher != null) {
                super.routeMatcher.clearRoutes();
            }
        }
    }

    private MatcherFilter matcherFilter;

    public MockServer() {
        SparkBaseMock sparkBaseMock = new SparkBaseMock();
        matcherFilter = new MatcherFilter(sparkBaseMock.getRoute(), false, false);
        matcherFilter.init((FilterConfig)null);
    }

    public void clear() {
        SparkBaseMock sparkBaseMock = new SparkBaseMock();
        sparkBaseMock.clearRoute();
    }

    public HttpServletResponse mockRequest(String method, String endpoint, String body) throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn(method);

        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))));
        when(request.getHeaders("Accept-Content")).thenReturn(new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        });
        when(request.getHeaders("Accept-Encoding")).thenReturn(new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        });

        StringBuilder string = new StringBuilder();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream()
        {

            @Override
            public void write(int b) throws IOException {
                string.append((char) b );
            }

            //Netbeans IDE automatically overrides this toString()
            public String toString(){
                return string.toString();
            }
        });

        final Integer retVal[] = new Integer[1];
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                retVal[0] = (Integer)invocation.getArguments()[0];
                return null;
            }
        }).when(response).setStatus(Mockito.any(Integer.class));
        matcherFilter.doFilter(request, response, null);

        when(response.getStatus()).thenReturn(Optional.ofNullable(retVal[0]).orElse(200));
        return response;
    }
}
