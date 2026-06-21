package com.busapp.busservice.service;

import com.busapp.busservice.dto.LayoutRequest;
import com.busapp.busservice.dto.LayoutResponse;

import java.util.List;

public interface LayoutService {
    List<LayoutResponse> getAllLayouts();
    LayoutResponse getLayoutById(Long id);
    LayoutResponse createLayout(LayoutRequest request);
    LayoutResponse updateLayout(Long id, LayoutRequest request);
    void deleteLayout(Long id);

    // ── Admin ──────────────────────────────────────────────────────────────────

    LayoutResponse getLayoutByName(String name);
}
