package com.hampcode.pagoya.integration.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hampcode.pagoya.auth.security.JwtAuthenticationFilter;
import com.hampcode.pagoya.auth.service.JwtService;
import com.hampcode.pagoya.shared.config.SecurityConfig;
import com.hampcode.pagoya.transfer.controller.TransferController;
import com.hampcode.pagoya.transfer.dto.TransferRequest;
import com.hampcode.pagoya.transfer.dto.TransferResponse;
import com.hampcode.pagoya.transfer.exception.InsufficientBalanceException;
import com.hampcode.pagoya.transfer.model.TransferStatus;
import com.hampcode.pagoya.transfer.model.TransferType;
import com.hampcode.pagoya.transfer.service.ITransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class TransferControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private ITransferService transferService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity()).build();
    }

    private TransferRequest internalRequest() {
        return new TransferRequest("SRC", TransferType.INTERNAL, "TGT",
            null, null, null, new BigDecimal("30.00"), "PEN");
    }

    private TransferResponse completed() {
        return new TransferResponse(1L, TransferType.INTERNAL, "SRC", "TGT",
            null, null, null, new BigDecimal("30.00"), "PEN", null,
            TransferStatus.COMPLETED, LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void transfer_authenticated_created() throws Exception {
        when(transferService.transfer(any())).thenReturn(completed());

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(internalRequest())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void transfer_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(internalRequest())))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void transfer_fromOtherUsersAccount_forbidden() throws Exception {
        when(transferService.transfer(any()))
            .thenThrow(new AccessDeniedException("denied"));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(internalRequest())))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void transfer_insufficientBalance_badRequest() throws Exception {
        when(transferService.transfer(any()))
            .thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(internalRequest())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void listByAccount_authenticated_ok() throws Exception {
        when(transferService.findByAccountNumber(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/transfers/account/ACC1"))
            .andExpect(status().isOk());
    }
}
