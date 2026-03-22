package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends CrudRepository<PurchaseOrder, Long> {

    /**
     * Do not add {@code productOrderList.product.machines} here: Hibernate throws
     * {@link org.hibernate.loader.MultipleBagFetchException} when two {@link java.util.List} bags
     * ({@code productOrderList} and {@code Product.machines}) are join-fetched together.
     * {@code Product.machines} is loaded lazily with batch fetching (see {@code Product}).
     */
    @Override
    @EntityGraph(attributePaths = {
            "customer",
            "productOrderList",
            "productOrderList.product"
    })
    List<PurchaseOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "customer",
            "productOrderList",
            "productOrderList.product"
    })
    Optional<PurchaseOrder> findById(Long id);

    boolean existsByCreatedAtBefore(LocalDateTime cutoff);
}

