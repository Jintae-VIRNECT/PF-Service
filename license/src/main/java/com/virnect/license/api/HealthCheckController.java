package com.virnect.license.api;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description application health check request controller
 * @since 2020.05.14
 */
@RestController
@RequestMapping("/")
public class HealthCheckController {
	@GetMapping("/healthcheck")
	public ResponseEntity<String> healthCheck() {
		String message =
			"\n\n"
				+ "------------------------------------------------------------------------------\n" + "\n"
				+ "   VIRNECT LICENSE SERVER\n"
				+ "   ---------------------------\n" + "\n"
				+ "   * SERVER_MODE: [ " + System.getenv("VIRNECT_ENV") + " ]\n" + "\n"
				+ "   * HEALTH_CHECK_DATE: [ " + ZonedDateTime.now() + " ]\n" + "\n"
				+ "------------------------------------------------------------------------------\n";
		return ResponseEntity.ok(message);
	}
}
