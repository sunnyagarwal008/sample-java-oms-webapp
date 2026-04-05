package com.example.ordermanagement;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CREATE_ORDER_JSON = """
            {
                "customerName": "John Doe",
                "customerEmail": "john@example.com",
                "items": [
                    {
                        "productName": "Widget",
                        "quantity": 2,
                        "unitPrice": 9.99
                    },
                    {
                        "productName": "Gadget",
                        "quantity": 1,
                        "unitPrice": 24.99
                    }
                ]
            }
            """;

    // --- Test 1: POST /api/orders (Create Order) ---

    @Test
    void createOrder_shouldReturn201WithOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.customerEmail").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(44.97));
    }

    @Test
    void createOrder_withMissingFields_shouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "customerName": "",
                    "customerEmail": "not-an-email",
                    "items": []
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // --- Test 2: GET /api/orders/{id} (Get Order by ID) ---

    @Test
    void getOrderById_shouldReturnOrder() throws Exception {
        // Create an order first
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_ORDER_JSON));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void getOrderById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString("999")));
    }

    // --- Test 3: PATCH /api/orders/{id}/status (Update Order Status) ---

    @Test
    void updateOrderStatus_shouldReturnUpdatedOrder() throws Exception {
        // Create an order first
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_ORDER_JSON));

        String statusUpdate = """
                { "status": "CONFIRMED" }
                """;

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateOrderStatus_notFound_shouldReturn404() throws Exception {
        String statusUpdate = """
                { "status": "SHIPPED" }
                """;

        mockMvc.perform(patch("/api/orders/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdate))
                .andExpect(status().isNotFound());
    }

    // --- Test 4: DELETE /api/orders/{id} (Delete Order) ---

    @Test
    void deleteOrder_shouldReturn204() throws Exception {
        // Create an order first
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_ORDER_JSON));

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/orders/999"))
                .andExpect(status().isNotFound());
    }
}
