package com.ecom.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class AssetOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderId;
    private LocalDate orderDate;

    @ManyToOne
    private Asset asset;

    private Integer price;

    @ManyToOne
    private User user;

    private String status;
    private String paymentType;

    public AssetOrder() {
    }

    public AssetOrder(String paymentType, String status, User user, Integer price, Asset asset, LocalDate orderDate, String orderId) {
        this.paymentType = paymentType;
        this.status = status;
        this.user = user;
        this.price = price;
        this.asset = asset;
        this.orderDate = orderDate;
        this.orderId = orderId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", orderDate=" + orderDate +
                ", asset=" + asset +
                ", price=" + price +
                ", user=" + user +
                ", status='" + status + '\'' +
                ", paymentType='" + paymentType + '\'' +
                '}';
    }
}
