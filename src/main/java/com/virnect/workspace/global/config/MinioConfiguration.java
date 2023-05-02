package com.virnect.workspace.global.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;

/**
 * Project: PF-Admin
 * DATE: 2020-03-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Profile({"local", "develop", "onpremise", "test", "freezing"})
@Configuration
public class MinioConfiguration {

	@Value("${minio.access-key}")
	private String accessKey;

	@Value("${minio.secret-key}")
	private String secretKey;

	@Value("${minio.server}")
	private String minioServer;

	@Bean
	public MinioClient minioClient() throws
		NoSuchAlgorithmException,
		KeyManagementException {
		MinioClient minioClient = MinioClient.builder()
			.endpoint(minioServer)
			.credentials(accessKey, secretKey)
			.build();
		minioClient.ignoreCertCheck();
		return minioClient;
	}
}
