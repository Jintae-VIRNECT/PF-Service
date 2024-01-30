package com.virnect.platform.workspace.global.validator;

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

public class ExtensionValidator implements ConstraintValidator<AllowedExtension, MultipartFile> {
	AllowedExtension.Extension[] allowedExtensions;

	@Override
	public void initialize(AllowedExtension allowedExtension) {
		allowedExtensions = allowedExtension.extensions();
	}

	@Override
	public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
		if (multipartFile == null || multipartFile.isEmpty()) {
			return true;
		}

		int dotIndex = multipartFile.getOriginalFilename().lastIndexOf('.');
		String extension = dotIndex == -1 ? "" : multipartFile.getOriginalFilename().substring(dotIndex + 1);

		return Arrays.stream(allowedExtensions).anyMatch(allowedExtension -> allowedExtension.name().equals(extension.toUpperCase()));
	}
}
