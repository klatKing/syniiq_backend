package com.syniiq.syniiq_backend.repository;

import com.syniiq.syniiq_backend.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
}