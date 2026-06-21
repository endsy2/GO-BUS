package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.dto.mapper.PaymentMapper;
import com.busapp.bookingservice.dto.request.PaymentRequest;
import com.busapp.bookingservice.dto.response.PaymentResponse;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.service.PaymentService;
import com.busapp.bookingservice.service.handler.PaymentHandler;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final Map<PaymentMethodType, PaymentHandler> handlerMap ;

    public PaymentServiceImpl(List<PaymentHandler> handlers,
                              PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        PaymentHandler::getType,
                        Function.identity()
                ));
    }

    @Override
    public PaymentResponse getPaymentsByBooking(Long bookingId) {
        return paymentMapper.toResponse(paymentRepository.findByBookingId(bookingId).orElseThrow(()->new ResourceNotFoundException("Payment not found for booking: " + bookingId)));
    }

    @Override
    public PaymentResponse getPaymentById(Long id, PaymentMethodType paymentMethodType) {
       PaymentHandler paymentHandler = handlerMap.get(paymentMethodType);
       if(paymentHandler==null){
           log.debug("PAYMENT_LOOKUP_REJECTED", kv("paymentId", id), kv("paymentMethodType", paymentMethodType),
                   kv("reason", "UNKNOWN_PAYMENT_TYPE"));
           throw new ResourceNotFoundException("Payment Type not found");
       }

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public void createPayment(Long id, PaymentRequest paymentRequest) {
        log.debug("PAYMENT_CREATE", kv("bookingId", id));
        Payment saved = paymentRepository.save(paymentMapper.toEntity(paymentRequest));
        log.debug("PAYMENT_CREATED", kv("bookingId", id), kv("paymentId", saved.getId()));
    }
}
