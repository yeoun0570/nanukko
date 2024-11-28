package nanukko.nanukko_back.service;

import lombok.AllArgsConstructor;
import nanukko.nanukko_back.domain.product.category.MajorCategory;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.repository.MajorCategoryRepository;
import nanukko.nanukko_back.repository.MiddleCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {
    private final MajorCategoryRepository majorCategoryRepository;
    private final MiddleCategoryRepository middleCategoryRepository;

    //대분류 겟
    public List<MajorCategory> getAllMajorCategories() {
        return majorCategoryRepository.findAll();
    }

    //대분류 존재?
    public boolean existsById(Long majorId) {
        return majorCategoryRepository.existsById(majorId);
    }

    //중분류 겟
    public List<MiddleCategory> getMiddleCategoriesByMajorId(Long majorId) {
        return middleCategoryRepository.findByMajor_MajorId(majorId);
    }
}
