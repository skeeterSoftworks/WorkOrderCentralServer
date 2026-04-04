package com.skeeterSoftworks.WorkOrderCentral.domain.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "products", "bookings", "machineImage" })
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "machines", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MachineBooking> bookings = new ArrayList<>();

    @Column
    private String machineName;

    @Column
    private String manufacturer;

    @Column
    private Integer manufactureYear;

    @Column
    private String internalNumber;

    @Column
    private String serialNumber;

    @Column
    private String location;

    /** Optional photo of the machine (or line); stored as raw bytes (e.g. JPEG/PNG). */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column
    private byte[] machineImage;

}
