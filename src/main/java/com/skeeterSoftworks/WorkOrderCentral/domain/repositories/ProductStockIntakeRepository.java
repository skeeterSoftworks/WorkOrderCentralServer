package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.ProductStockIntake;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductStockIntakeRepository extends CrudRepository<ProductStockIntake, Long> {

    List<ProductStockIntake> findAllByOrderByReceivedAtDescIdDesc(Pageable pageable);

    List<ProductStockIntake> findAllByReceivedAtGreaterThanEqualAndReceivedAtLessThanOrderByReceivedAtDescIdDesc(
            LocalDateTime receivedAtStartInclusive,
            LocalDateTime receivedAtEndExclusive,
            Pageable pageable);
}
