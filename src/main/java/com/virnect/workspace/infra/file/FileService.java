package com.virnect.workspace.infra.file;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

/**
 * Project: service-server
 * DATE: 2019-10-28
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: File Upload Interface for LocalFileUpload and AWS FileUpload
 */
public interface FileService {
    String upload(MultipartFile file, String workspaceUUID) throws IOException;

    void delete(final String url);

    String getDefaultFileUrl(DefaultImageFile defaultImageFile);
}
