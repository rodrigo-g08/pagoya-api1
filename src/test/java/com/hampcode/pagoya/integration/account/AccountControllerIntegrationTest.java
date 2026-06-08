package com.hampcode.pagoya.integration.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hampcode.pagoya.account.controller.AccountController;
import com.hampcode.pagoya.account.dto.AccountBalanceResponse;
import com.hampcode.pagoya.account.dto.AccountResponse;
import com.hampcode.pagoya.account.dto.CreateAccountRequest;
import com.hampcode.pagoya.account.dto.DepositRequest;
import com.hampcode.pagoya.account.model.AccountStatus;
import com.hampcode.pagoya.account.model.AccountType;
import com.hampcode.pagoya.account.service.IAccountService;
import com.hampcode.pagoya.auth.security.JwtAuthenticationFilter;
import com.hampcode.pagoya.auth.service.JwtService;
import com.hampcode.pagoya.shared.config.SecurityConfig;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AccountControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IAccountService accountService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity()).build();
    }

    private AccountResponse sampleAccount() {
        return new AccountResponse(1L, "ACC1", BigDecimal.TEN,
            AccountStatus.ACTIVE, AccountType.SAVINGS, 5L);
    }

    @Test
    @WithMockUser
    void createAccount_authenticated_created() throws Exception {
        when(accountService.create(any())).thenReturn(sampleAccount());

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAccountRequest(AccountType.SAVINGS))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountNumber").value("ACC1"));
    }

    @Test
    void createAccount_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAccountRequest(AccountType.SAVINGS))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void deposit_authenticated_ok() throws Exception {
        when(accountService.deposit(eq("ACC1"), any()))
            .thenReturn(new AccountBalanceResponse("ACC1", new BigDecimal("150.00")));

        mockMvc.perform(post("/api/accounts/ACC1/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new DepositRequest(new BigDecimal("50.00")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    void deposit_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/accounts/ACC1/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new DepositRequest(new BigDecimal("50.00")))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void closeAccount_authenticated_ok() throws Exception {
        when(accountService.close("ACC1")).thenReturn(sampleAccount());

        mockMvc.perform(patch("/api/accounts/ACC1/close"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void myAccounts_authenticated_ok() throws Exception {
        when(accountService.findMyAccounts(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/accounts/me"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void recipientByDni_authenticated_ok() throws Exception {
        when(accountService.findRecipientAccountsByDni("12345678")).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts/recipient").param("dni", "12345678"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listByCustomer_asNonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/accounts/customer/5"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getBalance_whenServiceDeniesAccess_forbidden() throws Exception {
        when(accountService.getBalance("ACC1"))
            .thenThrow(new AccessDeniedException("denied"));

        mockMvc.perform(get("/api/accounts/ACC1/balance"))
            .andExpect(status().isForbidden());
    }
}
