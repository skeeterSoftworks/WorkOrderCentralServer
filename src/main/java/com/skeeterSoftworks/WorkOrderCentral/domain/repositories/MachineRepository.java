package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Machine;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MachineRepository extends CrudRepository<Machine, Long> {

    List<Machine> findAll();
}
