package com.hampcode.pagoya.account.dto;

import com.hampcode.pagoya.account.model.AccountType;

/**
 * Vista mínima de una cuenta destino para elegir a quién transferir.
 * No expone saldo, id interno ni el DNI; el nombre va enmascarado para
 * confirmar al destinatario sin revelar datos sensibles.
 */
public record RecipientAccountResponse(
    String accountNumber,
    AccountType type,
    String ownerName
) {}
