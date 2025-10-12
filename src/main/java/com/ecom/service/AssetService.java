package com.ecom.service;

import com.ecom.model.Asset;
import com.ecom.model.Category;
import com.ecom.repo.AssetRepository;
import com.ecom.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.parameters.P;
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
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private CommonUtil commonUtil;

    // Retrieve all assets
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    // Retrieve all active assets
    public List<Asset> getAllActiveAssetsHome() {
        return assetRepository.findByActiveTrue();
    }


    // Add Asset
    public Asset addAsset(Asset asset, MultipartFile imageName, MultipartFile fileName) throws IOException {

//        String file1 = imageName.isEmpty() ? "default.jpg" : imageName.getOriginalFilename();
//        String file2 = fileName.isEmpty() ? "default.blend" : fileName.getOriginalFilename();

        String file1 = commonUtil.getImageUrl(imageName, 2);
        String file2 = commonUtil.getImageUrl(fileName, 2);

        asset.setImageName(file1);
        asset.setFileName(file2);

        Asset saveAsset = assetRepository.save(asset);

        if(!ObjectUtils.isEmpty(saveAsset)) {

            // Image Save
//            File saveFile1 = new ClassPathResource("static/images").getFile();
//            Path path1 = Paths.get(saveFile1.getAbsolutePath() + File.separator + "assets" + File.separator + imageName.getOriginalFilename());
//            Files.copy(imageName.getInputStream(), path1, StandardCopyOption.REPLACE_EXISTING);

            // Image Save - AWS S3
            s3Service.uploadFileS3(imageName, 2);

            // Asset File Save
//            File saveFile2 = new ClassPathResource("static/assets").getFile();
//            Path path2 = Paths.get(saveFile2.getAbsolutePath() + File.separator + fileName.getOriginalFilename());
//            Files.copy(fileName.getInputStream(), path2, StandardCopyOption.REPLACE_EXISTING);

            // Asset File Save - AWS S3
            s3Service.uploadFileS3(fileName, 2);
        }

        return saveAsset;
    }

    // Retrieve Asset By Id
    public Asset getAssetById(int id) {
        return assetRepository.findById(id).orElse(null);
    }

    // Update Asset
    public Asset updateAsset(Asset asset, MultipartFile imageName, MultipartFile fileName) throws IOException {

        Asset existingAsset = assetRepository.findById(asset.getId()).orElse(null);

//        String file1 = imageName.isEmpty() ? existingAsset.getImageName() : imageName.getOriginalFilename();
//        String file2 = fileName.isEmpty() ? existingAsset.getFileName() : fileName.getOriginalFilename();

        String file1 = imageName.isEmpty() ? existingAsset.getImageName() : commonUtil.getImageUrl(imageName, 2);
        String file2 = fileName.isEmpty() ? existingAsset.getFileName() : commonUtil.getImageUrl(fileName, 2);

        if(!ObjectUtils.isEmpty(existingAsset)) {
            existingAsset.setTitle(asset.getTitle());
            existingAsset.setDescription(asset.getDescription());
            existingAsset.setCategory(asset.getCategory());
            existingAsset.setPrice(asset.getPrice());
            existingAsset.setImageName(file1);
            existingAsset.setFileName(file2);
            existingAsset.setActive(asset.getActive());
        }

        Asset newAsset = assetRepository.save(existingAsset);

        if(!ObjectUtils.isEmpty(newAsset)) {

            if(!imageName.isEmpty()) {

                // Image Save
//                File saveFile1 = new ClassPathResource("static/images").getFile();
//                Path path1 = Paths.get(saveFile1.getAbsolutePath() + File.separator + "assets" + File.separator + imageName.getOriginalFilename());
//                Files.copy(imageName.getInputStream(), path1, StandardCopyOption.REPLACE_EXISTING);

                // Image Save - AWS S3
                s3Service.uploadFileS3(imageName, 2);
            }

            if(!fileName.isEmpty()) {

                // Asset File Save - Replace with AWS S3
//                File saveFile2 = new ClassPathResource("static/assets").getFile();
//                Path path2 = Paths.get(saveFile2.getAbsolutePath() + File.separator + fileName.getOriginalFilename());
//                Files.copy(fileName.getInputStream(), path2, StandardCopyOption.REPLACE_EXISTING);

                // Asset File Save - AWS S3
                s3Service.uploadFileS3(fileName, 2);

            }
        }

        return newAsset;

    }

    // Delete Asset By Id
    public Boolean deleteAsset(int id) {
        Asset asset = assetRepository.findById(id).orElse(null);

        if(!ObjectUtils.isEmpty(asset)) {
            assetRepository.delete(asset);
            return true;
        }
        return false;
    }

    // Retrieve All Active Assets
    public List<Asset> getAllActiveAssets(String category) {
        if(ObjectUtils.isEmpty(category)) {
            return assetRepository.findByActiveTrue();
        }

        return assetRepository.findByActiveTrueAndCategory(category);
    }

    // Retrieve All Assets By Search Active - fix active
    public List<Asset> searchAsset(String query) {
        return assetRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query);
    }

    // Retrieve All Assets By Search Active Pagination
    public Page<Asset> searchAssetActivePagination(String query, Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return assetRepository.findByActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query, pageable);
    }

    // Retrieve All Assets Active Pagination
    public Page<Asset> searchAllAssetsActivePagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return assetRepository.findByActiveTrue(pageable);
    }

    // Retrieve All Assets By Search Active Pagination
    public Page<Asset> searchAssetPagination(Integer pageNumber, Integer pageSize, String query) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return assetRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query, pageable);
    }

    // Retrieve All Assets By Category Pagination
    public Page<Asset> getAllActiveAssetsPagination(Integer pageNumber, Integer pageSize, String category) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Asset> pageAsset = null;

        if(ObjectUtils.isEmpty(category)) {
            pageAsset = assetRepository.findByActiveTrue(pageable);
        } else {
            pageAsset = assetRepository.findByActiveTrueAndCategory(pageable, category);
        }

        return pageAsset;
    }

    // Get All Assets Pagination
    public Page<Asset> getAllAssetsPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return assetRepository.findAll(pageable);
    }

}
