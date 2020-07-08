package com.virnect.license.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
        return ResponseEntity.ok("WELCOME LICENSE SERVER - " + LocalDateTime.now());
    }
}
