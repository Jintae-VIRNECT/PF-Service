package com.virnect.license.dto.rest.remote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileStorageInfoResponse {
	private String workspaceId;
	private long profileStorageSize;
	private long shareFileStorageSize;
	private long attachFileStorageSize;
	private long totalRemoteUseStorageSize;

	public FileStorageInfoResponse() {
	}

	private FileStorageInfoResponse(String workspaceId) {
		this.workspaceId = workspaceId;
	}

	public static FileStorageInfoResponse empty(String workspaceId) {
		return new FileStorageInfoResponse(workspaceId);
	}

	@Override
	public String toString() {
		return "FileStorageInfoResponse{" +
			"workspaceId='" + workspaceId + '\'' +
			", profileStorageSize=" + profileStorageSize +
			", shareFileStorageSize=" + shareFileStorageSize +
			", attachFileStorageSize=" + attachFileStorageSize +
			", totalRemoteUseStorageSize=" + totalRemoteUseStorageSize +
			'}';
	}
}
