package com.virnect.platform.global.middleware;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jeonghyeon.chang (johnmark)
 * @email practice1356@gmail.com
 * @since 2020.02.29
 */

@Component
@Aspect
@Slf4j
public class LoggerAspect {
	private final HttpServletRequest request;

	public LoggerAspect(HttpServletRequest request) {
		this.request = request;
	}

	private static JsonObject getParams(HttpServletRequest request) {
		JsonObject jsonObject = new JsonObject();
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String param = params.nextElement();
			String replaceParam = param.replaceAll("\\.", "-");
			jsonObject.addProperty(replaceParam, request.getParameter(param));
		}
		return jsonObject;
	}

	@Before("execution(* com.virnect.license.api.*Controller.*(..))")
	public void requestLogger(JoinPoint joinPoint) {
		String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		Map<String, Object> params = new HashMap<>();
		try {
			String userName = Optional.ofNullable(request.getHeader("X-jwt-name")).orElse("dW5rb3du");
			String authorization = String.format(
				"{UserName: [%s], UserEmail: [%s] UUID: [%s]}", new String(
					Base64.getDecoder().decode(userName.getBytes()), StandardCharsets.UTF_8),
				request.getHeader("X-jwt-email"), request.getHeader("X-jwt-uuid")
			);
			params.put("Authorization", authorization);
			params.put("Content-Type", request.getHeader("Content-Type"));
			params.put("RequestUri", request.getRequestURI());
			params.put("HttpMethod", request.getMethod());
			params.put("Controller", controllerName);
			params.put("Method", methodName);
			params.put("Params", getParams(request));
			params.put("Time", LocalDateTime.now());
		} catch (Exception e) {
			log.error("Logger Aspect Error", e);
		}

		log.info("[Request] => [{}]", params);
	}
}
