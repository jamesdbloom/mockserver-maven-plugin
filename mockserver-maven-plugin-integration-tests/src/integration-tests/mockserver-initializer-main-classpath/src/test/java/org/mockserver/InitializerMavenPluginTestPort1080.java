package org.mockserver;

import org.junit.Test;
import org.mockserver.client.netty.NettyHttpClient;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpStatusCode.OK_200;

/**
 * @author jamesdbloom
 */
public class InitializerMavenPluginTestPort1080 {

    private final static int SERVER_HTTP_PORT = 1080;
    protected List<String> headersToIgnore = Arrays.asList(
        "server",
        "expires",
        "date",
        "host",
        "connection",
        "user-agent",
        "content-type",
        "content-length",
        "accept-encoding",
        "transfer-encoding"
    );
    // http client
    private NettyHttpClient httpClient = new NettyHttpClient();

    @Test
    public void clientCanCallServer() {
        // then
        // - in http
        assertEquals(
            new HttpResponse()
                .withStatusCode(OK_200.code())
                .withReasonPhrase(OK_200.reasonPhrase())
                .withBody("test_initializer_response_body"),
            makeRequest(
                new HttpRequest()
                    .withMethod("POST")
                    .withPath("/test_initializer_path")
                    .withBody("test_initializer_request_body"),
                headersToIgnore
            )
        );
        // - in https
        assertEquals(
            new HttpResponse()
                .withStatusCode(OK_200.code())
                .withReasonPhrase(OK_200.reasonPhrase())
                .withBody("test_initializer_response_body"),
            makeRequest(
                new HttpRequest()
                    .withMethod("POST")
                    .withPath("/test_initializer_path")
                    .withBody("test_initializer_request_body"),
                headersToIgnore
            )
        );
    }

    protected HttpResponse makeRequest(HttpRequest httpRequest, Collection<String> headersToIgnore) {
        HttpResponse httpResponse = httpClient.sendRequest(httpRequest, new InetSocketAddress("localhost", SERVER_HTTP_PORT));
        List<Header> headers = new ArrayList<Header>();
        for (Header header : httpResponse.getHeaderList()) {
            if (!headersToIgnore.contains(header.getName().getValue().toLowerCase())) {
                headers.add(header);
            }
        }
        httpResponse.withHeaders(headers);
        return httpResponse;
    }

}
