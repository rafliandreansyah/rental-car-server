package com.rentalcar.server.restcontroller;

import com.rentalcar.server.model.DashboardResponse;
import com.rentalcar.server.model.base.WebResponse;
import com.rentalcar.server.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "App")
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @Operation(summary = "Get Dashboard Only Admin can access")
    @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebResponse<DashboardResponse>> getDashboard() {
        DashboardResponse dashboardResponse = appService.getDashboard();
        return ResponseEntity.ok().body(WebResponse.<DashboardResponse>builder()
                .status(HttpStatus.OK.value())
                .data(dashboardResponse)
                .build()
        );
    }

}
