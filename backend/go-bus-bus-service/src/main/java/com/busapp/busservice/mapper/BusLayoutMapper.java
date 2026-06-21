package com.busapp.busservice.mapper;

import com.busapp.busservice.dto.LayoutRequest;
import com.busapp.busservice.dto.LayoutResponse;
import com.busapp.busservice.model.BusLayout;
import org.springframework.stereotype.Component;

@Component
public class BusLayoutMapper {
    public LayoutResponse toResponse(BusLayout layout) {
        return LayoutResponse.builder()
                .id(layout.getId())
                .layout(layout.getLayout())
                .name(layout.getName())
                .description(layout.getDescription())
                .build();
    }
    public BusLayout toEntity(LayoutRequest layoutRequest) {
        return BusLayout.builder()
                .name(layoutRequest.getName())
                .layout(layoutRequest.getLayout())
                .description(layoutRequest.getDescription())
                .build();
    }
}
