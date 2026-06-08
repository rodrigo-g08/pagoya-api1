package com.hampcode.pagoya.transfer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_transfer_validations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExternalTransferValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", unique = true)
    private Transfer transfer;

    @Column(nullable = false, unique = true)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationResult result;

    @Column
    private String reason;

    @Column(nullable = false)
    private LocalDateTime validatedAt;
}
