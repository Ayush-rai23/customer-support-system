package com.support.backend.service;

import com.support.backend.dto.CategoryView;
import com.support.backend.dto.TicketMapper;
import com.support.backend.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryView> list() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(TicketMapper::toCategoryView)
                .toList();
    }
}
