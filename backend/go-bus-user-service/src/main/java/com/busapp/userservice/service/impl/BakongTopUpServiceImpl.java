package com.busapp.userservice.service.impl;

import com.busapp.userservice.config.BakongConfig;
import com.busapp.userservice.dto.request.CheckTopUpRequest;
import com.busapp.userservice.dto.request.TopUpBakongRequest;
import com.busapp.userservice.dto.response.BakongCheckTopUpResponse;
import com.busapp.userservice.dto.response.BakongQrData;
import com.busapp.userservice.dto.response.BakongResponse;
import com.busapp.userservice.dto.response.TopUpBakongResponse;
import com.busapp.userservice.exception.*;
import com.busapp.userservice.model.TopUp;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.model.WalletTransaction;
import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.model.enums.PaymentMethodType;
import com.busapp.userservice.model.enums.TopUpStatus;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.repository.TopUpRepository;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.repository.UserWalletRepository;
import com.busapp.userservice.repository.WalletTransactionRepository;
import com.busapp.userservice.service.BakongTopUpService;
import com.busapp.userservice.util.BakongUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.MerchantInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BakongTopUpServiceImpl implements BakongTopUpService {

    private final BakongConfig bakongConfig;
    private final UserRepository userRepository;
    private final UserWalletRepository walletRepository;
    private final TopUpRepository topUpRepository;
    private final WalletTransactionRepository transactionRepository;
    private final BakongUtil bakongTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final ObjectMapper objectMapper;

    @Value("${bakong.base-url}")
    private String baseUrl;

    @Value("${bakong.account-id}")
    private String bakongAccountId;

    @Value("${bakong.mobile-number}")
    private String bakongMobileNumber;

    // ─────────────────────────────────────────────
    // 1. Generate KHQR
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public BakongQrData generateTopUpKhqr(Long userId, TopUpBakongRequest request) {
        log.info("[BAKONG TOP-UP] Generating KHQR - userId={}, amount={}, currency={}",
                userId, request.getAmount(), Currency.USD);

        try {
            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG TOP-UP] User not found for KHQR generation - userId={}", userId);
                        return new ResourceNotFoundException("User not found with ID: " + userId);
                    });

            log.debug("[BAKONG TOP-UP] Found user - userId={}, userName={}", user.getId(), user.getUserName());

            // Validate wallet exists
            UserWallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG TOP-UP] Wallet not found for KHQR generation - userId={}", userId);
                        return new ResourceNotFoundException("Wallet not found for user ID: " + userId);
                    });

            log.debug("[BAKONG TOP-UP] Found wallet - walletId={}, currentBalance={}", 
                    wallet.getId(), wallet.getBalance());

            // Validate amount
            if (request.getAmount() == null || request.getAmount() <= 0) {
                log.error("[BAKONG TOP-UP] Invalid top-up amount - userId={}, amount={}", 
                        userId, request.getAmount());
                throw new BadRequestException("Top-up amount must be positive");
            }

            long deadline = System.currentTimeMillis() + bakongConfig.getConnectionTimeout();
            double amount = request.getAmount();

            log.debug("[BAKONG TOP-UP] Calculated amount - currency={}, amount={}", Currency.USD, amount);

            // Build merchant info
            String urlTemple = baseUrl.replaceAll("/+$", "") + "/bakong/generateQR";

            String url = UriComponentsBuilder
                    .fromHttpUrl(urlTemple)
                    .queryParam("amount", amount)
                    .queryParam("currency", "USD")
                    .queryParam("merchant_name", "CHIN KONGMING")
                    .queryParam("bank_account", bakongAccountId)
                    .queryParam("number_phone", bakongMobileNumber)
                    .toUriString();

            log.debug("[BAKONG] Generating KHQR - amount={}", amount);
            log.debug("URL :{}",url);

            BakongResponse response = restTemplate.getForObject(url, BakongResponse.class);
            log.debug("[BAKONG TOP-UP] Generating KHQR with merchant info - accountId={}, merchantName={}, amount={}",
                    bakongConfig.getAccountId(), bakongConfig.getMerchantName(), amount);

            // Validate response
            if (response == null || response.getData() == null) {
                log.error("[BAKONG TOP-UP] KHQR generation returned null response - userId={}", userId);
                throw new QRGenerationException("Failed to generate KHQR: null response from Bakong library");
            }

            // Extract MD5 from response
            log.info("response from bakong:{}",response);

            log.info("md5 debug:{}",response.getData());

            String json=objectMapper.writeValueAsString(response.getData());
            BakongQrData data = objectMapper.readValue(json, BakongQrData.class);

            String md5 = data.getMd5();
            if (md5 == null || md5.isBlank()) {
                log.error("[BAKONG TOP-UP] MD5 hash is null or empty - userId={}", userId);
                throw new QRGenerationException("Failed to get MD5 hash from KHQR response");
            }

            log.info("[BAKONG TOP-UP] KHQR generated successfully - userId={}, md5={}", userId, md5);

            // Create pending top-up record with the amount
            TopUp topUp = TopUp.builder()
                    .user(user)
                    .amount(amount)
                    .paymentMethod(PaymentMethodType.BAKONG)
                    .status(TopUpStatus.PENDING)
                    .paymentGateway("BAKONG_KHQR")
                    .transactionId(md5)
                    .build();

            topUpRepository.save(topUp);

            log.info("[BAKONG TOP-UP] Top-up record created - topUpId={}, userId={}, amount={}",
                    topUp.getId(), userId, amount);

            return data;

        } catch (ResourceNotFoundException | BadRequestException | QRGenerationException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG TOP-UP] Unexpected error during KHQR generation - userId={}, error={}",
                    userId, e.getMessage(), e);
            throw new QRGenerationException(
                    "Failed to generate KHQR for user ID: " + userId, e);
        }
    }

    // ─────────────────────────────────────────────
    // 2. Check Transaction (with Bakong API polling)
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public void checkTopUpTransaction(Long userId, CheckTopUpRequest checkTopUpRequest) {
        log.info("[BAKONG TOP-UP] Starting transaction check - userId={}, md5={}", 
                userId, checkTopUpRequest.getHash());

        try {
            // Validate inputs
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID");
            }

            if (checkTopUpRequest.getHash() == null || checkTopUpRequest.getHash().isBlank()) {
                throw new BadRequestException("MD5 hash is required");
            }

            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG TOP-UP] User not found - userId={}", userId);
                        return new ResourceNotFoundException("User not found with ID: " + userId);
                    });

            long deadline = System.currentTimeMillis() + bakongConfig.getConnectionTimeout();
            Long pollIntervalMs = bakongConfig.getPollingIntervalMs();

            log.debug("[BAKONG TOP-UP] Polling configuration - timeout={}ms, interval={}ms",
                    bakongConfig.getConnectionTimeout(), pollIntervalMs);

            // Pre-build URL and headers outside the loop
            String url = baseUrl.replaceAll("/+$", "") + "/bakong/verifyMD5";
            String bearerToken;


            log.debug("[BAKONG TOP-UP] Bakong API URL - {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, String> body = Map.of("md5", checkTopUpRequest.getHash());
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // Get or create top-up record ONCE before polling loop
            log.debug("[BAKONG TOP-UP] Getting or creating top-up record");
            TopUp topUp;
            try {
                topUp = getOrCreateTopUp(userId, checkTopUpRequest.getHash());
            } catch (Exception e) {
                log.error("[BAKONG TOP-UP] Failed to create top-up record - userId={}, error={}",
                        userId, e.getMessage(), e);
                throw new TransactionCheckException("Failed to find hash md5 top-up record", e);
            }

            // Polling loop
            int pollCount = 0;
            int maxRetries = 5;
            int consecutiveErrors = 0;

            log.info("[BAKONG TOP-UP] Starting polling loop - userId={}, md5={}", 
                    userId, checkTopUpRequest.getHash());

            while (System.currentTimeMillis() < deadline) {
                pollCount++;
                log.debug("[BAKONG TOP-UP] Poll attempt #{} - md5={}", pollCount, checkTopUpRequest.getHash());

                try {
                    ResponseEntity<String> upstream = restTemplate.exchange(
                            url, HttpMethod.POST, entity, String.class
                    );

                    // Reset error counter on successful request
                    consecutiveErrors = 0;

                    String responseBody = upstream.getBody();
                    if (responseBody == null || responseBody.isBlank()) {
                        log.warn("[BAKONG TOP-UP] Empty response from Bakong API - md5={}, attempt={}",
                                checkTopUpRequest.getHash(), pollCount);
                        Thread.sleep(pollIntervalMs);
                        continue;
                    }
                    log.debug("all response:{}", responseBody);
                    log.debug("[BAKONG TOP-UP] Received response - md5={}, body={}", 
                            checkTopUpRequest.getHash(), responseBody);

                    BakongCheckTopUpResponse response = mapper.readValue(responseBody, BakongCheckTopUpResponse.class);
                    log.debug("[BAKONG TOP-UP] Response status code - md5= BakongCheckTopUpResponse response = objectMapper.convertValue(bakongResponse.getData(), BakongCheckTopUpResponse.class);{}, status={}",
                            checkTopUpRequest.getHash(), response.getData().getStatus());

                    // Handle terminal states
                    if (response.getData().getStatus().equals("PAID")) {
                        log.info("[BAKONG TOP-UP] Payment SUCCESS - md5={}, userId={}",
                                checkTopUpRequest.getHash(), userId);
                        markSuccessAsync(topUp.getId());
                        return;
//                        case 15 -> {
//                            log.warn("[BAKONG TOP-UP] Payment FAILED - md5={}, userId={}",
//                                    checkTopUpRequest.getHash(), userId);
//                            markFailureAsync(TopUpStatus.FAILED, topUp.getId(), "Transaction failed");
//                            return;
//                        }
//                        case 46 -> {
//                            log.warn("[BAKONG TOP-UP] Payment EXPIRED - md5={}, userId={}",
//                                    checkTopUpRequest.getHash(), userId);
//                            markFailureAsync(TopUpStatus.EXPIRED, topUp.getId(), "Transaction expired");
//                            return;
//                        }
                    } else {// PENDING — wait and retry
                        log.debug("[BAKONG TOP-UP] Payment PENDING - md5={}, status={}, retrying in {}ms",
                                checkTopUpRequest.getHash(), response.getData().getStatus(), pollIntervalMs);
                        Thread.sleep(pollIntervalMs);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[BAKONG TOP-UP] Polling interrupted - md5={}, userId={}",
                            checkTopUpRequest.getHash(), userId);
                    markFailureAsync(TopUpStatus.FAILED, topUp.getId(), "Transaction check was interrupted");
                    throw new TransactionCheckException("Transaction check was interrupted", e);

                } catch (Exception e) {
                    consecutiveErrors++;
                    log.error("[BAKONG TOP-UP] Polling error - md5={}, attempt={}, consecutiveErrors={}, error={}",
                            checkTopUpRequest.getHash(), pollCount, consecutiveErrors, e.getMessage());

                    // If too many consecutive errors, fail fast
                    if (consecutiveErrors >= maxRetries) {
                        log.error("[BAKONG TOP-UP] Max consecutive errors reached - md5={}, userId={}",
                                checkTopUpRequest.getHash(), userId);
                        markFailureAsync(TopUpStatus.FAILED, topUp.getId(),
                            "Transaction check failed after " + maxRetries + " consecutive errors");
                        throw new BakongApiException(
                            "Bakong API unreachable after " + maxRetries + " attempts", e);
                    }

                    // Retry after delay
                    try {
                        Thread.sleep(pollIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[BAKONG TOP-UP] Sleep interrupted during error recovery - md5={}",
                                checkTopUpRequest.getHash());
                        markFailureAsync(TopUpStatus.FAILED, topUp.getId(), 
                            "Transaction check interrupted during error recovery");
                        throw new TransactionCheckException("Transaction check interrupted during error recovery", ie);
                    }
                }
            }

            // Deadline reached — mark as TIMEOUT
            log.warn("[BAKONG TOP-UP] Polling TIMEOUT reached - md5={}, userId={}, attempts={}",
                    checkTopUpRequest.getHash(), userId, pollCount);
            markFailureAsync(TopUpStatus.TIMEOUT, topUp.getId(), "Transaction check timed out");
            
            throw new PaymentTimeoutException(
                "Transaction check timed out after " + pollCount + " attempts for user ID: " + userId);

        } catch (ResourceNotFoundException | BadRequestException |
                 PaymentTimeoutException | BakongApiException | TransactionCheckException e) {
            // Re-throw known exceptions
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG TOP-UP] Unexpected error during transaction check - userId={}, error={}",
                    userId, e.getMessage(), e);
            throw new TransactionCheckException(
                    "Unexpected error checking transaction for user ID: " + userId, e);
        }
    }

    // ─────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────

    /**
     * Get existing top-up (should already exist from generateTopUpKhqr)
     */
    private TopUp getOrCreateTopUp(Long userId, String md5) {
        log.debug("[BAKONG TOP-UP] Getting top-up - userId={}, md5={}", userId, md5);
        
        return topUpRepository.findByTransactionId(md5)
                .orElseThrow(() -> {
                    log.error("[BAKONG TOP-UP] Top-up not found - md5={}", md5);
                    return new ResourceNotFoundException(
                            "Top-up not found for transaction ID: " + md5 + 
                            ". Please generate KHQR first.");
                });
    }

    /**
     * Mark success asynchronously
     */
    @Async
    @Transactional
    public void markSuccessAsync(Long topUpId) {
        log.debug("[BAKONG TOP-UP] Async markSuccess started - topUpId={}", topUpId);
        try {
            markSuccess(topUpId);
        } catch (Exception e) {
            log.error("[BAKONG TOP-UP] Error in async markSuccess - topUpId={}, error={}", 
                    topUpId, e.getMessage(), e);
        }
    }

    /**
     * Mark failure asynchronously
     */
    @Async
    @Transactional
    public void markFailureAsync(TopUpStatus status, Long topUpId, String reason) {
        log.debug("[BAKONG TOP-UP] Async markFailure started - topUpId={}, status={}, reason={}", 
                topUpId, status, reason);
        try {
            markFailure(status, topUpId, reason);
        } catch (Exception e) {
            log.error("[BAKONG TOP-UP] Error in async markFailure - topUpId={}, error={}", 
                    topUpId, e.getMessage(), e);
            throw new BadRequestException(reason);
        }
    }

    /**
     * Mark top-up as successful and update wallet
     */
    @Transactional
    public void markSuccess(Long topUpId) {
        log.info("[BAKONG TOP-UP] Marking top-up as SUCCESS - topUpId={}", topUpId);
        
        try {
            TopUp topUp = topUpRepository.findById(topUpId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG TOP-UP] Top-up not found - topUpId={}", topUpId);
                        return new ResourceNotFoundException("Top-up not found with ID: " + topUpId);
                    });
            
            log.debug("[BAKONG TOP-UP] Found top-up - topUpId={}, currentStatus={}", 
                    topUp.getId(), topUp.getStatus());
            
            // Check if already successful (idempotent)
            if (topUp.getStatus() == TopUpStatus.COMPLETED) {
                log.info("[BAKONG TOP-UP] Top-up already marked as COMPLETED - topUpId={}", topUp.getId());
                return;
            }
            
            // Get wallet
            UserWallet wallet = walletRepository.findByUserId(topUp.getUser().getId())
                    .orElseThrow(() -> {
                        log.error("[BAKONG TOP-UP] Wallet not found - userId={}", topUp.getUser().getId());
                        return new ResourceNotFoundException("Wallet not found for user");
                    });
            
            // Record balance before
            Double balanceBefore = wallet.getBalance();
            
            // Update wallet balance
            wallet.setBalance(balanceBefore + topUp.getAmount());
            wallet.setLastTransaction(LocalDateTime.now());
            walletRepository.save(wallet);
            log.debug("[BAKONG TOP-UP] Wallet balance updated - newBalance={}", wallet.getBalance());
            
            // Update top-up status
            topUp.setStatus(TopUpStatus.COMPLETED);
            topUp.setCompletedAt(LocalDateTime.now());
            topUpRepository.save(topUp);
            log.debug("[BAKONG TOP-UP] Top-up status updated to COMPLETED");
            
            // Create wallet transaction record
            WalletTransaction transaction = WalletTransaction.builder()
                    .wallet(wallet)
                    .amount(topUp.getAmount())
                    .type(TransactionType.TOP_UP)
                    .status(TransactionStatus.COMPLETED)
                    .referenceId(topUp.getTransactionId())
                    .description("Bakong QR Top-up")
                    .balanceBefore(balanceBefore)
                    .balanceAfter(wallet.getBalance())
                    .completedAt(LocalDateTime.now())
                    .metadata("{\"topUpId\": " + topUp.getId() + ", \"paymentMethod\": \"BAKONG\"}")
                    .build();
            
            transactionRepository.save(transaction);
            log.info("[BAKONG TOP-UP] Wallet transaction created - transactionId={}", transaction.getId());
            
            log.info("[BAKONG TOP-UP] Top-up marked as SUCCESS completed - topUpId={}, newBalance={}",
                    topUpId, wallet.getBalance());
                    
        } catch (ResourceNotFoundException e) {
            log.error("[BAKONG TOP-UP] Resource not found during markSuccess - topUpId={}", topUpId);
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG TOP-UP] Unexpected error during markSuccess - topUpId={}, error={}",
                    topUpId, e.getMessage(), e);
            throw new BakongPaymentException("Failed to mark top-up as successful for ID: " + topUpId, e);
        }
    }

    /**
     * Mark top-up as failed
     */
    @Transactional
    public void markFailure(TopUpStatus status, Long topUpId, String reason) {
        log.info("[BAKONG TOP-UP] Marking top-up as FAILED - topUpId={}, status={}, reason={}",
                topUpId, status, reason);
        
        try {
            TopUp topUp = topUpRepository.findById(topUpId)
                    .orElseThrow(() -> {
                        log.error("[BAKONG TOP-UP] Top-up not found - topUpId={}", topUpId);
                        return new ResourceNotFoundException("Top-up not found with ID: " + topUpId);
                    });
            
            log.debug("[BAKONG TOP-UP] Found top-up - topUpId={}, currentStatus={}", 
                    topUp.getId(), topUp.getStatus());
            
            // Check if already in terminal state (idempotent)
            if (topUp.getStatus() == TopUpStatus.COMPLETED) {
                log.warn("[BAKONG TOP-UP] Top-up already marked as COMPLETED, cannot mark as failed - topUpId={}", 
                        topUp.getId());
                return;
            }
            
            if (topUp.getStatus() == status) {
                log.info("[BAKONG TOP-UP] Top-up already marked as {} - topUpId={}", status, topUp.getId());
                return;
            }
            
            // Update top-up status
            topUp.setStatus(status);
            topUp.setCompletedAt(LocalDateTime.now());
            topUpRepository.save(topUp);
            log.debug("[BAKONG TOP-UP] Top-up status updated to {}", status);
            
            log.info("[BAKONG TOP-UP] Top-up marked as FAILED completed - topUpId={}, status={}",
                    topUpId, status);
                    
        } catch (ResourceNotFoundException e) {
            log.error("[BAKONG TOP-UP] Resource not found during markFailure - topUpId={}", topUpId);
            throw e;
        } catch (Exception e) {
            log.error("[BAKONG TOP-UP] Unexpected error during markFailure - topUpId={}, error={}",
                    topUpId, e.getMessage(), e);
            throw new BakongPaymentException("Failed to mark top-up as failed for ID: " + topUpId, e);
        }
    }
}
