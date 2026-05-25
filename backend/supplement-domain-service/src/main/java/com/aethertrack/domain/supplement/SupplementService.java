package com.aethertrack.domain.supplement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplementService {

    private final SupplementRepository repository;

    @Transactional(readOnly = true)
    public List<SupplementDto> findAll() {
        return repository.findAllByActiveTrue()
                         .stream()
                         .map(SupplementDto::from)
                         .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplementDto> findByCategory(String category) {
        return repository.findAllByCategoryIgnoreCaseAndActiveTrue(category)
                         .stream()
                         .map(SupplementDto::from)
                         .toList();
    }
}
