package com.virnect.platform.workspace.api;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-Workspace
 * @email practice1356@gmail.com
 * @description
 * @since 2020.05.08
 */

@RestController
@RequestMapping("/")
public class HealthController {
	@GetMapping("/healthCheck")
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("Workspace Server Health Check " + LocalDateTime.now());
	}
}
