package com.nitro.tech.cloud.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Kiểm tra sống (không cần API key / JWT)")
public class HealthController {

    @Operation(
            summary = "Health check",
            description = "Trả về `{ \"ok\": true }` khi process đang chạy. Dùng cho probe / load balancer.",
            security = {})
    @GetMapping("/health")
    public Map<String, Boolean> health() {
        return Map.of("ok", true);
    }
}
