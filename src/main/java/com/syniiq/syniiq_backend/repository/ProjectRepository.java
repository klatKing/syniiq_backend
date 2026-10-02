package com.syniiq.syniiq_backend.repository;

import com.syniiq.syniiq_backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}