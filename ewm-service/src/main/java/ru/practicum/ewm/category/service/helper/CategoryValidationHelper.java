package ru.practicum.ewm.category.service.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.dto.exception.NotFoundException;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryValidationHelper {
    private final CategoryRepository categoryRepository;

    public Category isCategoryPresent(Long catId) {
        Optional<Category> optionalCategory = categoryRepository.findById(catId);

        if (optionalCategory.isEmpty()) {
            log.error("Категория с ИД {} отсутствует в БД.", catId);
            throw new NotFoundException(String.format("Категория с ИД %d отсутствует в БД.", catId));
        }

        return optionalCategory.get();
    }
}
