package com.virnect.platform.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Project: PF-Admin
 * DATE: 2020-03-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Profile({"staging", "production"})
@Configuration
public class AWSConfiguration {

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Bean
	public BasicAWSCredentials basicAWSCredentials() {
		return new BasicAWSCredentials(accessKey, secretKey);
	}

	@Bean
	public AWSCredentialsProvider awsCredentialsProvider() {
		return new AWSStaticCredentialsProvider(basicAWSCredentials());
	}

	/**
	 * Amazon S3 Client
	 *
	 * @param awsCredentialsProvider - AWS Credential
	 * @return Amazon s3 Client Instance
	 */
	@Bean
	public AmazonS3 amazonS3Client(AWSCredentialsProvider awsCredentialsProvider) {
		AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(Regions.AP_NORTHEAST_2).build();
		return amazonS3Client;
	}
}
