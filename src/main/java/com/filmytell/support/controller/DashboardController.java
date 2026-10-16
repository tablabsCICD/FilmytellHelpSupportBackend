package com.filmytell.support.controller;

import com.filmytell.support.dto.common.ApiResponse;
import com.filmytell.support.dto.dashboard.DashboardSummaryResponse;
import com.filmytell.support.dto.dashboard.TeamWorkloadDto;
import com.filmytell.support.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Analytics", description = "Endpoints for management metrics, workload distribution, and SLA resolution tracking")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('REPORT_VIEW', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Get overall dashboard summary metrics (Status distribution, SLA breaches, AI resolution rate)")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboardSummary()));
    }

    @GetMapping("/team-workload")
    @PreAuthorize("hasAnyAuthority('REPORT_VIEW', 'ROLE_ADMIN', 'ROLE_SUPPORT_MANAGER')")
    @Operation(summary = "Get team and member workload distribution")
    public ResponseEntity<ApiResponse<List<TeamWorkloadDto>>> getTeamWorkload() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getTeamWorkloads()));
    }
}
