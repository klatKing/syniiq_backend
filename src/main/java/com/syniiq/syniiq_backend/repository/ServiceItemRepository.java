package com.syniiq.syniiq_backend.repository;

import com.syniiq.syniiq_backend.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
}