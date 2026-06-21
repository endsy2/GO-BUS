package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.LayoutRequest;
import com.busapp.busservice.dto.LayoutResponse;
import com.busapp.busservice.exception.BadRequestException;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.model.BusLayout;
import com.busapp.busservice.model.repository.BusLayoutRepository;
import com.busapp.busservice.model.repository.BusRepository;
import com.busapp.busservice.service.LayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LayoutServiceImpl implements LayoutService {

    private final BusLayoutRepository layoutRepository;
    private final BusRepository busRepository;

    @Override
    public List<LayoutResponse> getAllLayouts() {
        return layoutRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public LayoutResponse getLayoutById(Long id) {
        return toResponse(findLayout(id));
    }

    @Override
    @Transactional
    public LayoutResponse createLayout(LayoutRequest request) {
        // Check for duplicate layout name
        if (layoutRepository.findByName(request.getName()).isPresent()) {
            throw new com.busapp.busservice.exception.DuplicateResourceException(
                    "Layout with name '" + request.getName() + "' already exists");
        }
        
        BusLayout layout = BusLayout.builder()
                .name(request.getName())
                .layout(request.getLayout())
                .description(request.getDescription())
                .build();
        return toResponse(layoutRepository.save(layout));
    }

    @Override
    @Transactional
    public LayoutResponse updateLayout(Long id, LayoutRequest request) {
        BusLayout layout = findLayout(id);
        layout.setName(request.getName());
        layout.setLayout(request.getLayout());
        layout.setDescription(request.getDescription());
        return toResponse(layoutRepository.save(layout));
    }

    @Override
    @Transactional
    public void deleteLayout(Long id) {
        if (!layoutRepository.existsById(id)) {
            throw new ResourceNotFoundException("Layout not found: " + id);
        }
        if(busRepository.existsBusByLayoutId(id)){
            throw new BadRequestException("Layout with id '" + id + "' used");
        }

        layoutRepository.deleteById(id);
    }

    // ── Admin ──────────────────────────────────────────────────────────────────

    @Override
    public LayoutResponse getLayoutByName(String name) {
        BusLayout layout = layoutRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Layout not found with name: " + name));
        return toResponse(layout);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BusLayout findLayout(Long id) {
        return layoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Layout not found: " + id));
    }

    private LayoutResponse toResponse(BusLayout layout) {
        return LayoutResponse.builder()
                .id(layout.getId())
                .name(layout.getName())
                .layout(layout.getLayout())
                .description(layout.getDescription())
                .createdAt(layout.getCreatedAt())
                .build();
    }
}
