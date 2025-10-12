package com.ecom.service;


import com.ecom.model.Category;
import com.ecom.repo.CategoryRepository;
import com.ecom.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private CommonUtil commonUtil;

    // Add Category
    public Category addCategory(Category category, MultipartFile file) throws IOException {

//        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";

        String file1 = commonUtil.getImageUrl(file, 1);

        category.setImageName(file1);

        Boolean categoryExists = categoryRepository.existsByName(category.getName());

        if(categoryExists){
            return null;
        }

        Category returnCategory = categoryRepository.save(category);

        if(!ObjectUtils.isEmpty(returnCategory)){

            // Category Image
//            File saveFile = new ClassPathResource("static/images").getFile();
//            // Make path
//            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category" + File.separator + file.getOriginalFilename());
//            // Save file
//            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Category Image - AWS S3
            s3Service.uploadFileS3(file, 1);

        }

        return returnCategory;
    }

    // Retrieve ALl Category
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Update Category
    public Category updateCategory(Category category, MultipartFile file) throws IOException {

        Category existingCategory = categoryRepository.findById(category.getId()).orElse(null);
//        String imageName = file.isEmpty() ? existingCategory.getImageName() : file.getOriginalFilename();

        String imageName = file.isEmpty() ? existingCategory.getImageName() : commonUtil.getImageUrl(file, 1);

        if(!ObjectUtils.isEmpty(existingCategory)) {
            existingCategory.setName(category.getName());
            existingCategory.setActive(category.isActive());
            existingCategory.setImageName(imageName);
        }

        Category newCategory = categoryRepository.save(existingCategory);

        if(!ObjectUtils.isEmpty(newCategory)){

            if(!file.isEmpty()) {

                // Category Image
//                File saveFile = new ClassPathResource("static/images").getFile();
//                // Make path
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category" + File.separator + file.getOriginalFilename());
//                // Save file
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Category Image - AWS S3
                s3Service.uploadFileS3(file, 1);
            }
        }

        return newCategory;
    }

    // Delete Category
    public Boolean deleteCategory(int id) {
        Category category = categoryRepository.findById(id).orElse(null);

        if(!ObjectUtils.isEmpty(category)) {
            categoryRepository.delete(category);
            return true;
        }
        return false;
    }

    // Retrieve Category By Id
    public Category findCategoryById(int id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return category;
    }

    // Retrieve All Active Categories
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    // Retrieve All Categories Pagination
    public Page<Category> getAllActiveCategoriesPagination(Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Category> pageAsset = null;

        pageAsset = categoryRepository.findAll(pageable);

        return pageAsset;
    }

}
