package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.sla.SlaPolicyRequest;
import com.filmytell.support.dto.sla.SlaPolicyResponse;
import com.filmytell.support.service.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla-policies")
@RequiredArgsConstructor
@Tag(name = "SLA Policies", description = "Endpoints for configuring and inspecting SLA rules")
public class SlaPolicyController {

    private final SlaService slaService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('REPORT_VIEW', 'ROLE_ADMIN')")
    @Operation(summary = "Get all configured SLA policies")
    public ResponseEntity<ApiResponse<List<SlaPolicyResponse>>> getAllPolicies() {
        return ResponseEntity.ok(ApiResponse.ok(slaService.getAllPolicies()));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create a new SLA policy")
    public ResponseEntity<ApiResponse<SlaPolicyResponse>> createPolicy(@Valid @RequestBody SlaPolicyRequest request) {
        SlaPolicyResponse response = slaService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("SLA policy created successfully", response));
    }
}
