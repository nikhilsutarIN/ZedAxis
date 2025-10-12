package com.ecom.service;

import com.ecom.model.*;
import com.ecom.repo.CartRepository;
import com.ecom.repo.OrderRepository;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;

    public List<String> saveOrder(String paymentType, Integer userId) {

        List<Cart> cartList = cartRepository.findByUserId(userId);

        List<String> orderIdList = new ArrayList<>();

        for(Cart c: cartList) {

            AssetOrder order = new AssetOrder();
            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());
            order.setAsset(c.getAsset());
            order.setPrice(c.getAsset().getPrice());
            order.setUser(c.getUser());
            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(paymentType);

            AssetOrder savedOrder = orderRepository.save(order);

            orderIdList.add(savedOrder.getOrderId());

        }

        return orderIdList;
    }

    public void updateOrderStatusByOrderIdSuccess(String orderId) {
        AssetOrder order = orderRepository.findByOrderIdCustom(orderId);
        order.setStatus(OrderStatus.FULFILLED.getName());
        AssetOrder savedOrder = orderRepository.save(order);

        try {
            commonUtil.sendMailForModelPurchase(savedOrder, "Fulfilled");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateOrderStatusByOrderIdFail(String orderId) {
        AssetOrder order = orderRepository.findByOrderIdCustom(orderId);
        order.setStatus(OrderStatus.CANCELLED.getName());
        AssetOrder savedOrder = orderRepository.save(order);

        try {
            commonUtil.sendMailForModelPurchase(savedOrder, "Failed");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Boolean cancelOrder(Integer id) {
        AssetOrder order = orderRepository.findById(id).orElse(null);

        if(!ObjectUtils.isEmpty(order)) {
            order.setStatus(OrderStatus.CANCELLED.getName());
            orderRepository.save(order);
            return true;
        } else {
            return false;
        }

    }

    public List<AssetOrder> getOrdersByUserId(Integer userId) {

        return orderRepository.findByUserId(userId);

    }

    public AssetOrder updateOrderStatus(Integer id, String status) {
        AssetOrder order = orderRepository.findById(id).orElse(null);

        if(!ObjectUtils.isEmpty(order)) {

            order.setStatus(status);
            return orderRepository.save(order);
        }

        return null;

    }

    public List<AssetOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<AssetOrder> getOrderByUserIdAndAssetId(Integer assetId, Integer userId) {
        return orderRepository.findByUserIdAndAssetId(userId, assetId);
    }

    public List<AssetOrder> getOrderBySearch(String query) {
        return orderRepository.findByOrderId(query);

    }

    public Page<AssetOrder> getOrderBySearchPagination(String query, Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<AssetOrder> page = orderRepository.findByOrderId(query, pageable);

        return page;
    }

    public Page<AssetOrder> getAllOrdersPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<AssetOrder> page = orderRepository.findAll(pageable);

        return page;
    }
}
