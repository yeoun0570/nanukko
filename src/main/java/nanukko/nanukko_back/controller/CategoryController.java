package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.product.category.MajorCategory;
import nanukko.nanukko_back.domain.product.category.MiddleCategory;
import nanukko.nanukko_back.exception.CategoryExceptions;
import nanukko.nanukko_back.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/categories/")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/major")
    public ResponseEntity<List<MajorCategory>> getMajorCategories() {
        List<MajorCategory> majorCategories = categoryService.getAllMajorCategories();
        log.info("Major Categories: {}", majorCategories);  // 로그 추가
        return ResponseEntity.ok(majorCategories);
    }

    @GetMapping("/middle/{majorId}")
    public ResponseEntity<List<MiddleCategory>> getMiddleCategories(@PathVariable Long majorId) {
//        if (!categoryService.existsById(majorId)) {
//            throw CategoryExceptions.NOT_FOUND.get();
//        }
        List<MiddleCategory> middleCategories = categoryService.getMiddleCategoriesByMajorId(majorId);
        log.info("Middle Categories for majorId {}: {}", majorId, middleCategories);  // 로그 추가
        return ResponseEntity.ok(middleCategories);
    }
}
