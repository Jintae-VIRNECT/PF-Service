package com.virnect.license.infra.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-User
 * @email practice1356@gmail.com
 * @description Local File Upload Service
 * @since 2020.03.19
 */
public interface FileUploadService {
    /**
     * 파일 업로드 처리
     * @param file - 파일 업로드 요청
     * @return - 업로드된 파일 url
     */
    String upload(MultipartFile file) throws IOException;

    /**
     * 파일 업로드 처리
     * @param file - 업로드 요청 파일
     * @param fileName - 파일 저장명
     * @return - 업로드된 파일 url
     * @throws IOException
     */
    String upload(MultipartFile file, String fileName) throws IOException;

    /**
     * 업로드 된 파일 삭제 요청
     * @param url - 업로드된 파일 url
     */
    boolean delete(final String url);

    /**
     *  업로드 요청 파일 확장자 추출
     * @param originFileName - 업로드 요청 파일 원본 이름
     * @return - 업로드 요청 파일 확장자
     */
    String getFileExtension(final String originFileName);

    /**
     * 업로드 요청 파일 확장자 허용 여부 검사
     * @param fileExtension - 업로드 요청 파일 확장자
     * @return - 허용 여부
     */
    boolean isAllowFileExtension(String fileExtension);

    /**
     * 파일 읽어들이기
     * @param url - 파일이 저장된 경로
     * @return - 파일 데이터
     */
    File getFile(final String url);
}
