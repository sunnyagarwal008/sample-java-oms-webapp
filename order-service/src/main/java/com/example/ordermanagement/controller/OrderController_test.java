// --- Test 5: GET /api/orders (Get All Orders) ---

@Test
void getAllOrders_shouldReturnEmptyList() throws Exception {
    mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
}

@Test
void getAllOrders_shouldReturnListOfOrders() throws Exception {
    // Create first order
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    // Create second order
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
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].customerName").value("John Doe"))
            .andExpect(jsonPath("$[1].customerName").value("Jane Smith"));
}

@Test
void getAllOrders_withMultipleStatuses_shouldReturnAllOrders() throws Exception {
    // Create an order
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    // Update its status
    String statusUpdate = """
            { "status": "SHIPPED" }
            """;

    mockMvc.perform(patch("/api/orders/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(statusUpdate));

    // Create another order (will have PENDING status)
    String secondOrderJson = """
            {
                "customerName": "Alice Brown",
                "customerEmail": "alice@example.com",
                "items": [
                    {
                        "productName": "Laptop",
                        "quantity": 1,
                        "unitPrice": 999.99
                    }
                ]
            }
            """;

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(secondOrderJson));

    mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].status").value("SHIPPED"))
            .andExpected(jsonPath("$[1].status").value("PENDING"));
}

// --- Additional Test for PATCH /api/orders/{id}/status (Update Order Status) ---

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
void updateOrderStatus_withEmptyRequest_shouldReturn400() throws Exception {
    // Create an order first
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(CREATE_ORDER_JSON));

    String emptyStatusUpdate = """
            { }
            """;

    mockMvc.perform(patch("/api/orders/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(emptyStatusUpdate))
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