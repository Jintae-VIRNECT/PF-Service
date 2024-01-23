package com.virnect.license.global.middleware;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class FilterServletOutputStream extends ServletOutputStream {
	private final DataOutputStream outputStream;

	public FilterServletOutputStream(OutputStream output) {
		this.outputStream = new DataOutputStream(output);
	}

	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener listener) {
	}
}
