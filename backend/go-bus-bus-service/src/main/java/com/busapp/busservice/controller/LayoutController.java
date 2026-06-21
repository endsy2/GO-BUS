package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.LayoutRequest;
import com.busapp.busservice.dto.LayoutResponse;
import com.busapp.busservice.service.LayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/layouts")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<LayoutResponse>>> getAllLayouts() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Layouts retrieved successfully",
                layoutService.getAllLayouts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LayoutResponse>> getLayoutById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Layout retrieved successfully",
                layoutService.getLayoutById(id)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /** Look up a layout by its unique name. */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<LayoutResponse>> getLayoutByName(
            @PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Layout retrieved successfully",
                layoutService.getLayoutByName(name)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LayoutResponse>> createLayout(
            @Valid @RequestBody LayoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Layout created successfully",
                        layoutService.createLayout(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LayoutResponse>> updateLayout(
            @PathVariable Long id,
            @Valid @RequestBody LayoutRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Layout updated successfully",
                layoutService.updateLayout(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLayout(@PathVariable Long id) {
        layoutService.deleteLayout(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Layout deleted successfully", null));
    }
}
