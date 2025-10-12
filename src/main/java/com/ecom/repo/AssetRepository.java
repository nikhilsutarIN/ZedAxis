package com.ecom.repo;

import com.ecom.model.Asset;
import com.ecom.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {

    public List<Asset> findByActiveTrue();

    public List<Asset> findByActiveTrueAndCategory(String category);

    public List<Asset> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String query, String query1);

    public Page<Asset> findByActiveTrue(Pageable pageable);

    public Page<Asset> findByActiveTrueAndCategory(Pageable pageable, String category);

    public Page<Asset> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String query, String query1, Pageable pageable);

    public Page<Asset> findByActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String query, String query1, Pageable pageable);


}
