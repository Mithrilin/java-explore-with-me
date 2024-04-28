package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.dto.ewm_service.category.CategoryDto;
import ru.practicum.ewm.dto.ewm_service.category.NewCategoryDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category newCategoryDtoToCategory(NewCategoryDto newCategoryDto);

    CategoryDto categoryToCategoryDto(Category category);

    List<CategoryDto> categoryListToCategoryDtoList(List<Category> categoryList);
}
