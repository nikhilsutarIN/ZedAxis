package com.ecom.controller;

import com.ecom.model.*;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.IndianStatesMap;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OrderService orderService;

    // Executed when controller is loaded
    // Principal = currently logger user
    @ModelAttribute
    public void getUserDetails(Principal p, Model model) {

        // Add User in model
        if (p != null) {
            String email = p.getName();
            User user = userService.getUserByEmail(email);
            model.addAttribute("user", user);
            Integer count = cartService.getCartCount(user.getId());
            model.addAttribute("cartCount", count);
        }

        // Add Categories in model
        List<Category> activeCategories = categoryService.getAllActiveCategories();
        model.addAttribute("activeCategories", activeCategories);
    }

    @GetMapping("/profile")
    public String profile(Principal p, Model model) {

        User user = getLoggedUser(p);
        model.addAttribute("user", user);

        LinkedHashMap<String, String> states = IndianStatesMap.getStatesLinkedHashMap();
        String state = user.getState();
        String newState = states.get(state);

        model.addAttribute("newState", newState);

        return "user/profile";
    }

    @GetMapping("/updateUser")
    public String updateUser(Principal p, Model model) {

        User user = getLoggedUser(p);
        model.addAttribute("user", user);

        LinkedHashMap<String, String> states = IndianStatesMap.getStatesLinkedHashMap();
        model.addAttribute("states", states);

        return "user/update_user";
    }

    @PostMapping("/updateUser")
    public String updateUserDetails(@ModelAttribute User user, HttpSession session) {

        User savedUser = userService.updateUserDetails(user);

        if(!ObjectUtils.isEmpty(savedUser)) {
            session.setAttribute("message", "User details have been updated");
        } else {
            session.setAttribute("error", "User details have not been updated");
        }

        return "redirect:/user/updateUser";
    }


    @GetMapping("/addCart")
    public String addCart(@RequestParam int aid, @RequestParam int uid, HttpSession session) {

        Cart cart = cartService.saveCart(aid, uid);

        if(ObjectUtils.isEmpty(cart)) {
            session.setAttribute("error", "Could not add model to cart");
        } else {
            session.setAttribute("message", "Model has been added to cart");
        }
        return "redirect:/models/" + aid;
    }

    private User getLoggedUser(Principal p) {

        String email = p.getName();
        User user = userService.getUserByEmail(email);

        return user;
    }

    @GetMapping("/cart")
    public String showCart(Principal p, Model model) {

        User user = getLoggedUser(p);

        List<Cart> cartList = cartService.findByUser(user.getId());
        model.addAttribute("cartList", cartList);

        if(!cartList.isEmpty()) {
            Cart lastCart = cartList.get(cartList.size() - 1);
            model.addAttribute("totalPrice", lastCart.getTotalPrice());
        }

        Integer count = cartService.getCartCount(user.getId());
        model.addAttribute("cartCount", count);

        return "user/view_cart";
    }

    @GetMapping("/cart/delete/{assetId}")
    public String deleteCart(@PathVariable Integer assetId, Principal p, HttpSession session) {

        User user = getLoggedUser(p);

//        Boolean status = cartService.deleteCartItem(assetId, user.getId());

        cartService.deleteCartItem(assetId, user.getId());

//        if(status) {
//            session.setAttribute("message", "Item removed from cart");
//        } else {
//            session.setAttribute("error", "Could not remove item from cart");
//        }

        return "redirect:/user/cart";
    }

    @GetMapping("/orders")
    public String showOrder(Principal p, Model model) {

        User user = getLoggedUser(p);

        Integer count = cartService.getCartCount(user.getId());

        if(count <= 0) {
            return "redirect:/user/cart";
        }
        model.addAttribute("cartCount", count);

        model.addAttribute("user", user);

        List<Cart> cartList = cartService.findByUser(user.getId());

        if(!cartList.isEmpty()) {
            Cart lastCart = cartList.get(cartList.size() - 1);
            model.addAttribute("totalPrice", lastCart.getTotalPrice());
        }

        return "user/order";
    }

    @PostMapping("/orders")
    public String addOrder(@RequestParam String email, @RequestParam String paymentType, HttpSession session) {

//        System.out.println("Order Request: " + orderRequest.toString());

        User existingUser = userService.getUserByEmail(email);

        List<String> orderIdList = orderService.saveOrder(paymentType, existingUser.getId());

        session.setAttribute("orderIdList", orderIdList);
        session.setAttribute("userId", existingUser.getId());

        return "redirect:/user/payment";
//        return "redirect:/user/order-success";
    }

    @GetMapping("/payment")
    public String payment(HttpSession session) {
        if(!ObjectUtils.isEmpty(session.getAttribute("orderIdList"))) {
            return "user/payment";
        }
        return "redirect:/user/orders";
    }

    @GetMapping("/paymentSuccess")
    public String paymentSuccess(HttpSession session) {
        List<String> orderIdList = (List<String>) session.getAttribute("orderIdList");

        if(!ObjectUtils.isEmpty(orderIdList)) {
            for(String orderId : orderIdList) {
                orderService.updateOrderStatusByOrderIdSuccess(orderId);

            }
        }

        Integer userId = (Integer) session.getAttribute("userId");

        cartService.clearCart(userId);

        session.removeAttribute("userId");
        session.removeAttribute("orderIdList");

        return "redirect:/user/order-success";
    }

    @GetMapping("/paymentFail")
    public String paymentFail(HttpSession session) {

        List<String> orderIdList = (List<String>) session.getAttribute("orderIdList");

        if(!ObjectUtils.isEmpty(orderIdList)) {
            for(String orderId : orderIdList) {
                orderService.updateOrderStatusByOrderIdFail(orderId);
            }
        }

        Integer userId = (Integer) session.getAttribute("userId");

        cartService.clearCart(userId);

        session.removeAttribute("userId");
        session.removeAttribute("orderIdList");

        return "redirect:/user/order-failed";

    }

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "user/orderSuccess";
    }

    @GetMapping("/order-failed")
    public String orderFailed() {
        return "user/orderFailed";
    }

    @GetMapping("/order")
    public String order(Principal p, Model model) {

        User user = getLoggedUser(p);

        List<AssetOrder> orderList = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("orderList", orderList);

        return "user/view_order";
    }

    @GetMapping("/orderCancel/{id}")
    public String orderCancel(@PathVariable Integer id, HttpSession session) {

        Boolean status = orderService.cancelOrder(id);

        if(status) {
            session.setAttribute("message", "Order has been cancelled");
        } else {
            session.setAttribute("error", "Order could not be cancelled");
        }
        return "redirect:/user/order";
    }

    // Not used
    @GetMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

        OrderStatus[] values = OrderStatus.values();
        String status = null;

        for(OrderStatus value : values) {
            if(value.getId().equals(st)) {
                status = value.getName();
            }
        }

        AssetOrder updateOrderStatus = orderService.updateOrderStatus(id, status);

        if(!ObjectUtils.isEmpty(updateOrderStatus)) {
            session.setAttribute("message", "Order status updated");

            try {
                commonUtil.sendMailForModelPurchase(updateOrderStatus, status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            session.setAttribute("error", "Order status not updated");
        }

        return "redirect:/user/order";
    }

}
