package ru.practicum.ewm.category.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.service.helper.CategoryValidationHelper;
import ru.practicum.ewm.dto.ewm_service.category.CategoryDto;
import ru.practicum.ewm.dto.ewm_service.category.NewCategoryDto;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final CategoryValidationHelper categoryValidationHelper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.newCategoryDtoToCategory(newCategoryDto);
        Category returnedCategory = categoryRepository.save(category);
        CategoryDto returnedCategoryDto = categoryMapper.categoryToCategoryDto(returnedCategory);
        log.info("Добавлена новая категория с ID = {}", returnedCategoryDto.getId());
        return returnedCategoryDto;
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        categoryValidationHelper.isCategoryPresent(catId);
        categoryRepository.deleteById(catId);
        log.info("Категория с ID {} удалена.", catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, CategoryDto categoryDto) {
        Category category = categoryValidationHelper.isCategoryPresent(catId);
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        CategoryDto updatedCategoryDto = categoryMapper.categoryToCategoryDto(updatedCategory);
        log.info("Категория с ID {} изменена.", catId);
        return updatedCategoryDto;
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageRequest);

        if (categoryPage.isEmpty()) {
            log.info("Не нашлось категорий по заданным параметрам.");
            return new ArrayList<>();
        }

        List<Category> categoryList = categoryPage.getContent();
        List<CategoryDto> categoryDtoList = categoryMapper.categoryListToCategoryDtoList(categoryList);
        log.info("Список категорий с номера {} размером {} возвращён.", from, categoryDtoList.size());
        return categoryDtoList;
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        Category returnedCategory = categoryValidationHelper.isCategoryPresent(catId);
        CategoryDto returnedCategoryDto = categoryMapper.categoryToCategoryDto(returnedCategory);
        log.info("Категория с ID = {} возвращена", returnedCategoryDto.getId());
        return returnedCategoryDto;
    }
}
