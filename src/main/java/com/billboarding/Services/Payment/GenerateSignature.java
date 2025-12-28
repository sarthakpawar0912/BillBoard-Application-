package com.billboarding.Services.Payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class GenerateSignature {

    public static void main(String[] args) throws Exception {

        String orderId = "order_RvMz8pkJm3OKRp"; // SAME AS CREATE ORDER
        String paymentId = "pay_mock987654";
        String secret = "Umse7jXNrEHMVVw8gNMf2DDa";

        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(key);

        byte[] digest = mac.doFinal(payload.getBytes());

        // HEX (not Base64)
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }

        System.out.println("MOCK SIGNATURE = " + hex);
    }
}
