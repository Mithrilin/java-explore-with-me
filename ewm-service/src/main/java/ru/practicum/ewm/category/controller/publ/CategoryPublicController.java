package ru.practicum.ewm.category.controller.publ;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.dto.ewm_service.category.CategoryDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") @Min(0) int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable @Positive long catId) {
        return categoryService.getCategoryById(catId);
    }
}
