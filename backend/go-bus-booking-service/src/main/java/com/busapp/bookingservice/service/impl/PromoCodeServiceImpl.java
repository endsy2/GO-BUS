package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.dto.request.PromoCodeRequest;
import com.busapp.bookingservice.dto.response.PromoCodeResponse;
import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.exception.DuplicateResourceException;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.model.PromoCode;
import com.busapp.bookingservice.repository.PromoCodeRepository;
import com.busapp.bookingservice.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeRepository promoRepository;

    @Override
    public List<PromoCodeResponse> getAllPromos() {
        return promoRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PromoCodeResponse getPromoById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public PromoCodeResponse getPromoByCode(String code) {
        PromoCode promo = promoRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + code));
        return toResponse(promo);
    }

    @Override
    @Transactional
    public PromoCodeResponse createPromo(PromoCodeRequest request) {
        log.debug("PROMO_CREATE", kv("code", request.getCode()), kv("discountType", request.getDiscountType()),
                kv("discountValue", request.getDiscountValue()));
        if (promoRepository.findByCode(request.getCode()).isPresent()) {
            log.debug("PROMO_CREATE_REJECTED", kv("code", request.getCode()), kv("reason", "CODE_EXISTS"));
            throw new DuplicateResourceException("Promo code already exists: " + request.getCode());
        }
        PromoCode promo = PromoCode.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxUses(request.getMaxUses())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .status(request.getStatus())
                .build();
        PromoCode saved = promoRepository.save(promo);
        log.debug("PROMO_CREATED", kv("promoId", saved.getId()), kv("code", saved.getCode()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PromoCodeResponse updatePromo(Long id, PromoCodeRequest request) {
        log.debug("PROMO_UPDATE", kv("promoId", id));
        PromoCode promo = findById(id);
        promo.setCode(request.getCode());
        promo.setDescription(request.getDescription());
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        promo.setMaxUses(request.getMaxUses());
        promo.setValidFrom(request.getValidFrom());
        promo.setValidTo(request.getValidTo());
        promo.setStatus(request.getStatus());
        PromoCodeResponse response = toResponse(promoRepository.save(promo));
        log.debug("PROMO_UPDATED", kv("promoId", id), kv("code", response.getCode()));
        return response;
    }

    @Override
    @Transactional
    public void deletePromo(Long id) {
        log.debug("PROMO_DELETE", kv("promoId", id));
        if (!promoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promo not found: " + id);
        }
        promoRepository.deleteById(id);
        log.debug("PROMO_DELETED", kv("promoId", id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PromoCode findById(Long id) {
        return promoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo not found: " + id));
    }

    private PromoCodeResponse toResponse(PromoCode p) {
        return PromoCodeResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .description(p.getDescription())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .maxUses(p.getMaxUses())
                .usedCount(p.getUsedCount())
                .validFrom(p.getValidFrom())
                .validTo(p.getValidTo())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
