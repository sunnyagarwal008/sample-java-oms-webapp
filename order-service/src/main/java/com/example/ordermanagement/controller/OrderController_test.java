// --- Test 5: GET /api/orders (Get All Orders) ---

@Test
void getAllOrders_shouldReturnEmptyListWhenNoOrders() throws Exception {
    mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpected(jsonPath("$", hasSize(0)));
}

@Test
void getAllOrders_shouldReturnAllOrders() throws Exception {
    // Create two orders
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    String secondOrderJson = """
            {
                "customerName": "Jane Smith",
                "customerEmail": "jane@example.com",
                "items": [
                    {
                        "productName": "Book",
                        "quantity": 1,
                        "unitPrice": 15.99
                    }
                ]
            }
            """;

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(secondOrderJson));

    mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpected(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].customerName").value("John Doe"))
            .andExpect(jsonPath("$[1].customerName").value("Jane Smith"));
}

// --- Additional PATCH /api/orders/{id}/status tests ---

@Test
void updateOrderStatus_withInvalidStatus_shouldReturn400() throws Exception {
    // Create an order first
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    String invalidStatusUpdate = """
            { "status": "INVALID_STATUS" }
            """;

    mockMvc.perform(patch("/api/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidStatusUpdate))
            .andExpect(status().isBadRequest());
}

@Test
void updateOrderStatus_withEmptyBody_shouldReturn400() throws Exception {
    // Create an order first
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    mockMvc.perform(patch("/api/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isBadRequest());
}

@Test
void updateOrderStatus_toDelivered_shouldReturnUpdatedOrder() throws Exception {
    // Create an order first
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    String statusUpdate = """
            { "status": "DELIVERED" }
            """;

    mockMvc.perform(patch("/api/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(statusUpdate))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("DELIVERED"))
            .andExpect(jsonPath("$.customerName").value("John Doe"));
}