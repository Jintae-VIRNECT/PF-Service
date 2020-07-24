package com.virnect.license.global.common;

import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @project: PF-Auth
 * @author: jeonghyeon.chang (johnmark)
 * @email: practice1356@gmail.com
 * @since: 2020.03.09
 * @description: AES 256 관련 암복호화 유틸
 */
@Slf4j
public class AES256Utils {
    // Util class do not have public constructor
    private AES256Utils() {
    }

    public static String encrypt(final String key, final String data) {
        try {
            byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
            byte[] ivData = key.substring(0, 16).getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(keyData, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(ivData));

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            log.error("AES Util Encrypt Error: {}", e.getMessage());
            e.printStackTrace();
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
    }

    public static String decrypt(final String key, final String encryptedData) {
        byte[] keyData;
        try {
            keyData = key.getBytes(StandardCharsets.UTF_8);
            byte[] ivData = key.substring(0, 16).getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(keyData, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivData));
            byte[] decrypted = Base64.getDecoder().decode(encryptedData.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(decrypted), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            log.error("AES Util Decrypt Error: {}", e.getMessage());
            e.printStackTrace();
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
    }
}
