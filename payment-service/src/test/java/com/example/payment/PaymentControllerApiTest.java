package com.example.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CREATE_PAYMENT_JSON = """
            {
                "orderId": 1,
                "amount": 49.99,
                "paymentMethod": "CREDIT_CARD",
                "customerEmail": "john@example.com"
            }
            """;

    @Test
    void createPayment_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_PAYMENT_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.amount").value(49.99))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.transactionReference").isNotEmpty());
    }

    @Test
    void createPayment_withInvalidData_shouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "orderId": null,
                    "amount": -10,
                    "paymentMethod": "CREDIT_CARD",
                    "customerEmail": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentById_shouldReturnPayment() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYMENT_JSON));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void getPaymentById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString("999")));
    }

    @Test
    void getPaymentsByOrderId_shouldReturnList() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYMENT_JSON));

        mockMvc.perform(get("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(1));
    }

    @Test
    void completePayment_shouldUpdateStatus() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYMENT_JSON));

        mockMvc.perform(post("/api/payments/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void completePayment_alreadyCompleted_shouldReturn409() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYMENT_JSON));

        mockMvc.perform(post("/api/payments/1/complete"));

        mockMvc.perform(post("/api/payments/1/complete"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("not in PENDING")));
    }

    @Test
    void refundPayment_shouldUpdateStatus() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYMENT_JSON));

        mockMvc.perform(post("/api/payments/1/complete"));

        mockMvc.perform(post("/api/payments/1/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "Customer requested refund" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void refundPayment_notCompleted_shouldReturn409() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_PAYMENT_JSON));

        mockMvc.perform(post("/api/payments/1/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "Customer requested refund" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("Only completed")));
    }
}
