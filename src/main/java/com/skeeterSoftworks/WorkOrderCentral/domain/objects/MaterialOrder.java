package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EMaterialOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"materialProvider", "lines"})
public class MaterialOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Server-generated order number, e.g. NM053120261101 (NM + MMddyyyyHHmm). */
    @Column(unique = true, length = 24)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_provider_id", referencedColumnName = "id")
    private MaterialProvider materialProvider;

    @OneToMany(mappedBy = "materialOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<MaterialOrderLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EMaterialOrderStatus status;

    /** Server time when {@link #status} was last changed (including creation). */
    @Column
    private LocalDateTime lastChanged;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime rejectedAt;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column
    private byte[] certificate;

    /** Derived flag for search/sort; certificate is stored as PostgreSQL bytea. */
    @Formula("(case when certificate is not null and length(certificate) > 0 then true else false end)")
    private boolean certificatePresent;
}
