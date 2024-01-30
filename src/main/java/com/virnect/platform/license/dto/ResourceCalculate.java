package com.virnect.platform.license.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResourceCalculate {
	private long totalCallTime;
	private long totalStorageSize;
	private long totalDownloadHit;

	@Override
	public String toString() {
		return "ResourceCalculate{" +
			"totalCallTime=" + totalCallTime +
			", totalStorageSize=" + totalStorageSize +
			", totalDownloadHit=" + totalDownloadHit +
			'}';
	}
}
