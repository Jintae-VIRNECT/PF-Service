package com.virnect.license.global.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.AES256Utils;
import com.virnect.license.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpServletRequestBodyDecodingWrapper extends HttpServletRequestWrapper {
    private final String decodingBody;
    private final Charset encoding;
    private byte[] rawData;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public HttpServletRequestBodyDecodingWrapper(HttpServletRequest request, final String decryptSecretKey) {
        super(request);
        String characterEncoding = request.getCharacterEncoding();
        if (StringUtils.isBlank(characterEncoding)) {
            characterEncoding = StandardCharsets.UTF_8.name();
        }
        this.encoding = Charset.forName(characterEncoding);

        // Convert InputStream data to byte array and store it to this wrapper instance.
        try {
            InputStream inputStream = request.getInputStream();
            this.rawData = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("[BILLING] - REQUEST INPUT STREAM READ FAIL.");
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        EncodingRequestResponse encodingRequestResponse = null;
        try {
            encodingRequestResponse = objectMapper.readValue(this.rawData, EncodingRequestResponse.class);
            log.info("[ENCODING_REQUEST] - {}", encodingRequestResponse.toString());
        } catch (IOException e) {
            log.error("[BILLING] - REQUEST CONVERT OBJECT FAIL.");
            e.printStackTrace();
        }
        String url = request.getRequestURI();
        log.info("[BILLING][ENCRYPT][REQUEST][{}] - [{}]", url, encodingRequestResponse.getData());
        String decode = AES256Utils.decrypt(decryptSecretKey, encodingRequestResponse.getData());
        log.info("[BILLING][DECRYPT][REQUEST][{}] - [{}]", url, decode);
        this.decodingBody = decode;
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodingBody.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}
