package com.virnect.workspace.infra.file;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.error.ErrorCode;

/**
 * Project: PF-Admin
 * DATE: 2020-03-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Profile({"staging", "production"})
@Slf4j
@Service
public class S3FileService implements FileService {

	@Value("${cloud.aws.s3.bucket.name}")
	private String bucket;

	@Value("${cloud.aws.s3.prefix}")
	private String prefix;

	@Value("${cloud.aws.s3.bucket.extension}")
	private String allowExtension;

	private AmazonS3 amazonS3Client;

	public S3FileService(AmazonS3 amazonS3) {
		this.amazonS3Client = amazonS3;
	}

	@Override
	public String upload(MultipartFile file, String workspaceUUID) {
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
		String objectName = String.format("workspace/%s/profile/%s", workspaceUUID, uniqueFileName);
		log.info(
			"[FILE UPLOAD] Upload File Info >> bucket : {}, object : {}, fileSize : {}", bucket, objectName,
			file.getSize()
		);

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		objectMetadata.setContentLength(file.getSize());
		objectMetadata.setHeader("filename", uniqueFileName);
		objectMetadata.setContentDisposition(String.format("attachment; filename=\"%s\"", uniqueFileName));

		try {
			PutObjectRequest putObjectRequest = new PutObjectRequest(
				bucket, objectName, file.getInputStream(), objectMetadata);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);

			amazonS3Client.putObject(putObjectRequest);
			String uploadPath = amazonS3Client.getUrl(bucket, objectName).toExternalForm();
			log.info("[FILE UPLOAD] Upload Result path : [{}],", uploadPath);
			return uploadPath;

		} catch (IOException e) {
			log.error(e.getMessage());
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

	}

	@Override
	public void delete(String fileUrl) {
		if (!StringUtils.hasText(fileUrl)) {
			return;
		}
		String[] fileSplit = fileUrl.split(prefix);
		String objectName = fileSplit[fileSplit.length - 1];

		if (fileUrl.contains(DefaultImageFile.WORKSPACE_PROFILE_IMG.getName())) {
			log.info("Not Delete Default File Info >> bucket : {}, object : {}", bucket, objectName);
		} else {
			amazonS3Client.deleteObject(bucket, objectName);
			log.info("Delete File Info >> bucket : {}, object : {}", bucket, objectName);
		}

	}

	@Override
	public String getDefaultFileUrl(DefaultImageFile defaultImageFile) {
		return prefix + "workspace/" + defaultImageFile.getName();
	}
}
