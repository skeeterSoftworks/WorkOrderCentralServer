package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EEmailTemplateCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private EEmailTemplateCode code;

    @Column(nullable = false, length = 500)
    private String subjectTemplate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;
}
