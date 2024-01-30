package com.virnect.platform.global.middleware;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.global.common.AES256Utils;
import com.virnect.platform.global.error.ErrorCode;
import com.virnect.platform.license.exception.BillingServiceException;

@Slf4j
public class RequestBodyDecodingWrapper extends HttpServletRequestWrapper {
	private final String decodingBody;

	/**
	 * Constructs a request object wrapping the given request.
	 *
	 * @param request The request to wrap
	 * @throws IllegalArgumentException if the request is null
	 */
	public RequestBodyDecodingWrapper(HttpServletRequest request, final String decryptSecretKey) {
		super(request);
		// Convert InputStream data to byte array and store it to this wrapper instance.
		byte[] rawData;
		try {
			InputStream inputStream = request.getInputStream();
			rawData = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			log.error("[BILLING] - REQUEST INPUT STREAM READ FAIL.");
			throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			EncodingRequestResponse encodingRequestResponse = objectMapper.readValue(
				rawData,
				EncodingRequestResponse.class
			);
			log.info("[ENCODING_REQUEST] - {}", encodingRequestResponse.toString());
			String url = request.getRequestURI();
			log.info("[BILLING][ENCRYPT][REQUEST][{}] - [{}]", url, encodingRequestResponse.getData());
			String decode = AES256Utils.decrypt(decryptSecretKey, encodingRequestResponse.getData());
			log.info("[BILLING][DECRYPT][REQUEST][{}] - [{}]", url, decode);
			this.decodingBody = decode;
		} catch (Exception e) {
			log.error("[BILLING] - REQUEST CONVERT OBJECT FAIL.");
			log.error(e.getMessage(), e);
			throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
	}

	@Override
	public ServletInputStream getInputStream() {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
			decodingBody.getBytes(StandardCharsets.UTF_8));
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
