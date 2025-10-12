package com.ecom.repo;

import com.ecom.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Cart findByAssetIdAndUserId(Integer assetId, Integer userId);

    public List<Cart> findByUserId(Integer userId);

    public Integer countByUserId(Integer userId);

    public void deleteByAssetIdAndUserId(Integer assetId, Integer userId);

    public void deleteByUserId(Integer userId);

}
