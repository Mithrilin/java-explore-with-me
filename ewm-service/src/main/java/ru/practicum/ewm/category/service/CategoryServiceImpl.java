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
import ru.practicum.ewm.dto.ewm_service.category.CategoryDto;
import ru.practicum.ewm.dto.ewm_service.category.NewCategoryDto;
import ru.practicum.ewm.dto.exception.NotFoundException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        Category returnedCategory = categoryRepository.save(category);
        CategoryDto returnedCategoryDto = categoryMapper.toCategoryDto(returnedCategory);
        log.info("Добавлена новая категория с ID = {}", returnedCategoryDto.getId());
        return returnedCategoryDto;
    }

    @Override
    public void deleteCategory(long catId) {
        isCategoryPresent(catId);
        categoryRepository.deleteById(catId);
        log.info("Категория с ID {} удалена.", catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, CategoryDto categoryDto) {
        Category category = isCategoryPresent(catId);
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        CategoryDto updatedCategoryDto = categoryMapper.toCategoryDto(updatedCategory);
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
        List<CategoryDto> categoryDtoList = categoryMapper.toCategoryDtoList(categoryList);
        log.info("Список категорий с номера {} размером {} возвращён.", from, categoryDtoList.size());
        return categoryDtoList;
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        Category returnedCategory = isCategoryPresent(catId);
        CategoryDto returnedCategoryDto = categoryMapper.toCategoryDto(returnedCategory);
        log.info("Категория с ID = {} возвращена", returnedCategoryDto.getId());
        return returnedCategoryDto;
    }

    private Category isCategoryPresent(long catId) {
        Optional<Category> optionalCategory = categoryRepository.findById(catId);
        if (optionalCategory.isEmpty()) {
            log.error("Категория с ИД {} отсутствует в БД.", catId);
            throw new NotFoundException(String.format("Категория с ИД %d отсутствует в БД.", catId));
        }
        return optionalCategory.get();
    }
}
