package com.syniiq.syniiq_backend.repository;

import com.syniiq.syniiq_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}