package com.ecom.service;

import com.ecom.model.Asset;
import com.ecom.model.Cart;
import com.ecom.model.User;
import com.ecom.repo.AssetRepository;
import com.ecom.repo.CartRepository;
import com.ecom.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    public Cart saveCart(Integer aid, Integer uid) {

        User user = userRepository.findById(uid).orElse(null);
        Asset asset = assetRepository.findById(aid).orElse(null);

        Cart cartStatus = cartRepository.findByAssetIdAndUserId(aid, uid);

        Cart cart = null;

        if(ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setAsset(asset);
            cart.setUser(user);
            cart.setTotalPrice(asset.getPrice());

        } else {
            cart = cartStatus;
            cart.setTotalPrice(asset.getPrice());
        }

        Cart saveCart = cartRepository.save(cart);
        return saveCart;
    }

    public List<Cart> findByUser(Integer userId) {

        List<Cart> cartList = cartRepository.findByUserId(userId);

        Integer totalPrice = 0;

        for(Cart c: cartList) {
            totalPrice = totalPrice + c.getAsset().getPrice();
            c.setTotalPrice(totalPrice);
        }

        return cartList;
    }

    public Integer getCartCount(Integer userId) {
        Integer count = cartRepository.countByUserId(userId);
        return count;
    }

    @Transactional
    public Boolean deleteCartItem(Integer assetId, Integer userId) {

        Cart cart = cartRepository.findByAssetIdAndUserId(assetId, userId);

        if(!ObjectUtils.isEmpty(cart)) {
            cartRepository.deleteByAssetIdAndUserId(assetId, userId);
            return true;
        }

        return false;
    }

    @Transactional
    public void clearCart(Integer userId) {
       cartRepository.deleteByUserId(userId);
    }

}
