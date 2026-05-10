package com.skeeterSoftworks.WorkOrderCentral.domain.repositories;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.EmailTemplate;
import com.skeeterSoftworks.WorkOrderCentral.to.enums.EEmailTemplateCode;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EmailTemplateRepository extends CrudRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByCode(EEmailTemplateCode code);

    List<EmailTemplate> findAllByOrderByCodeAsc();
}
