package com.billboarding.Services.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayPayoutService {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    @Value("${razorpay.account_number:}")
    private String accountNumber;

    @Value("${razorpay.test_mode:true}")
    private boolean testMode;

    private static final String RAZORPAY_PAYOUT_URL = "https://api.razorpay.com/v1/payouts";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Create a payout to owner's fund account via RazorpayX API
     * In TEST MODE: Returns a mock payout ID (no actual transfer)
     * In LIVE MODE: Calls actual RazorpayX API
     *
     * @param fundAccountId - Razorpay fund account ID (fa_xxxxx)
     * @param amount - Amount in rupees
     * @param reference - Reference ID for tracking
     * @return Razorpay payout ID
     */
    public String createPayout(String fundAccountId, Double amount, String reference) {

        // TEST MODE: Simulate payout without actual API call
        if (testMode || keyId.startsWith("rzp_test")) {
            log.info("TEST MODE: Simulating payout - Amount: {}, FundAccount: {}, Reference: {}",
                    amount, fundAccountId, reference);
            String mockPayoutId = "pout_test_" + UUID.randomUUID().toString().substring(0, 14);
            log.info("TEST MODE: Generated mock payout ID: {}", mockPayoutId);
            return mockPayoutId;
        }

        // LIVE MODE: Actual RazorpayX API call
        try {
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("account_number", accountNumber);
            payload.put("fund_account_id", fundAccountId);
            payload.put("amount", Math.round(amount * 100)); // Convert to paise
            payload.put("currency", "INR");
            payload.put("mode", "IMPS");
            payload.put("purpose", "payout");
            payload.put("reference_id", reference);
            payload.put("narration", "Billboard Owner Payout");
            payload.put("queue_if_low_balance", true);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    RAZORPAY_PAYOUT_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }

            throw new RuntimeException("Payout creation failed");

        } catch (Exception e) {
            log.error("Payout failed: {}", e.getMessage());
            throw new RuntimeException("Payout failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get payout status
     * In TEST MODE: Returns "processed" status
     * In LIVE MODE: Fetches actual status from RazorpayX
     */
    public String getPayoutStatus(String payoutId) {

        // TEST MODE: Return mock status
        if (testMode || keyId.startsWith("rzp_test") || payoutId.startsWith("pout_test_")) {
            log.info("TEST MODE: Returning mock status 'processed' for payout: {}", payoutId);
            return "processed";
        }

        // LIVE MODE: Actual API call
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    RAZORPAY_PAYOUT_URL + "/" + payoutId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("status");
            }

            throw new RuntimeException("Failed to fetch payout status");

        } catch (Exception e) {
            log.error("Failed to fetch payout status: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch payout status", e);
        }
    }

    /**
     * Check if running in test mode
     */
    public boolean isTestMode() {
        return testMode || keyId.startsWith("rzp_test");
    }

    /**
     * Create Basic Auth headers for Razorpay API
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = keyId + ":" + keySecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}
