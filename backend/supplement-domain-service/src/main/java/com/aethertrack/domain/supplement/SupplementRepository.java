package com.aethertrack.domain.supplement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplementRepository extends JpaRepository<Supplement, Long> {

    List<Supplement> findAllByActiveTrue();

    List<Supplement> findAllByCategoryIgnoreCaseAndActiveTrue(String category);
}
