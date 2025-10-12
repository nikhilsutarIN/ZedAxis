package com.ecom.repo;

import com.ecom.model.AssetOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<AssetOrder,Integer> {

    public List<AssetOrder> findByUserId(Integer userId);

    public List<AssetOrder> findByUserIdAndAssetId(Integer userId, Integer assetId);

    public List<AssetOrder> findByOrderId(String orderId);

    public Page<AssetOrder> findByOrderId(String query, Pageable pageable);

    @Query("SELECT o FROM AssetOrder o where o.orderId = ?1")
    public AssetOrder findByOrderIdCustom(String orderId);

}
