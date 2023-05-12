package com.virnect.workspace.global.validator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({ElementType.FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ExtensionValidator.class)
public @interface AllowedExtension {
	String message() default "is not allowed extension.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Extension[] extensions() default {};

	enum Extension {PNG, JPG, ICO}
}
