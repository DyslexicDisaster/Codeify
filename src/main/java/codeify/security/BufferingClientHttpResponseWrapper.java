package codeify.security;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for {@link ClientHttpResponse} that buffers the response body.
 * This is useful when you need to read the response body multiple times.
 */
public class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse delegate;
    private final byte[] body;

    /**
     * Constructs a new BufferingClientHttpResponseWrapper.
     *
     * @param delegate the original ClientHttpResponse
     * @param body     the buffered response body
     */
    public BufferingClientHttpResponseWrapper(ClientHttpResponse delegate, byte[] body) {
        this.delegate = delegate;
        this.body     = body;
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(body);
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return (HttpStatus) delegate.getStatusCode();
    }

    @SuppressWarnings("removal")
    @Override
    public int getRawStatusCode() throws IOException {
        return delegate.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return delegate.getStatusText();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public void close() {
        delegate.close();
    }
}