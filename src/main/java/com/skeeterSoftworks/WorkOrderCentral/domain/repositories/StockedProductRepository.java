package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.StockedProduct;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockedProductRepository extends CrudRepository<StockedProduct, Long> {

    Optional<StockedProduct> findByProduct_Id(Long productId);

    @Query("SELECT COALESCE(SUM(sp.quantity), 0) FROM StockedProduct sp WHERE sp.product.id = :productId")
    long sumQuantityByProductId(@Param("productId") Long productId);
}
