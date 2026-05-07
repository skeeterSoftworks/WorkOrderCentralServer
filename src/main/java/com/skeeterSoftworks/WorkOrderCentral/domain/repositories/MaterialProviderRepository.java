package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MaterialProviderRepository extends CrudRepository<MaterialProvider, Long> {
    List<MaterialProvider> findAll();
}
