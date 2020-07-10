package com.virnect.license.global.middleware;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseBodyEncryptWrapper extends HttpServletResponseWrapper {
    ByteArrayOutputStream output;
    FilterServletOutputStream filterOutput;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response The response to be wrapped
     * @throws IllegalArgumentException if the response is null
     */
    public ResponseBodyEncryptWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (filterOutput == null) {
            filterOutput = new FilterServletOutputStream(output);
        }
        return filterOutput;
    }

    public byte[] getDataStream() {
        return output.toByteArray();
    }
}
