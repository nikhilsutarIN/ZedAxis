package com.ecom.controller;

import com.ecom.model.Asset;
import com.ecom.model.AssetOrder;
import com.ecom.model.Category;
import com.ecom.service.*;
import com.ecom.util.IndianStatesMap;
import com.ecom.model.User;
import com.ecom.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    @Autowired
    private AssetService  assetService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

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

    @GetMapping("/")
    public String index(Model model) {
        List<Category> allCategories = categoryService.getAllActiveCategories().stream().limit(6).toList();
        List<Asset> allAssets = assetService.getAllActiveAssetsHome().stream()
                .sorted((a1, a2) -> a2.getId().compareTo(a1.getId()))
                .limit(8)
                .toList();

        model.addAttribute("allCategories", allCategories);
        model.addAttribute("allAssets", allAssets);

        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        LinkedHashMap<String, String> states = IndianStatesMap.getStatesLinkedHashMap();
        model.addAttribute("states", states);
        return "register";
    }

    @PostMapping("/saveUser")
    public String saveUser(@Valid @ModelAttribute User user, BindingResult result, HttpSession session, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {

        if(result.hasErrors()){
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            session.setAttribute("error", errorMessage);
            return "redirect:/register";
        }

        User savedUser = userService.saveUser(user);

        if(!ObjectUtils.isEmpty(savedUser)) {
            // Generate Url
            String url = commonUtil.generateUrl(request) + "/activate?token=" + savedUser.getActivateToken();

            // Send activation email
            Boolean status = commonUtil.sendActivationMail(user.getEmail(), url, user.getName());

            if(status) {
                session.setAttribute("message", "Registration successful! Please verify your email");
            } else {
                session.setAttribute("error", "Registration successful but failed to send activation email");
            }

        } else {
            session.setAttribute("error", "Email already exists");
        }

        return "redirect:/register";
    }

    // Activation endpoint - handles email verification
    @GetMapping("/activate")
    public String activateAccount(@RequestParam String token, Model model) {

        if(token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Activation link is invalid");
            return "message";
        }

        User existingUser = userService.getUserByActivateToken(token);

        if(ObjectUtils.isEmpty(existingUser)) {
            model.addAttribute("error", "Activation link is invalid or expired");
            return "message";
        }

        // Check if user is already enabled
        if(existingUser.getIsEnabled()) {
            model.addAttribute("message", "Your account is already activated. You can login now.");
            return "message";
        }

        // Activate user account
        userService.activateUser(existingUser);

        model.addAttribute("message", "Account activated successfully! You can now login to your account.");
        return "message";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/forgot")
    public String forgot() {
        return "forgot_password";
    }

    @PostMapping("/forgot")
    public String forgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        User existingUser = userService.getUserByEmail(email);

        if(ObjectUtils.isEmpty(existingUser)) {
            session.setAttribute("error", "Invalid email");
        } else {

            String reset_token = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, reset_token);

            // Generate URL
            // http://localhost:8080/reset-password?token=sfasdfsdfds-sdfsdfaf-sfdsfd
            String url = commonUtil.generateUrl(request) + "/reset?token=" + reset_token;

            Boolean sendMail = commonUtil.sendMail(email, url);

            if(sendMail) {
                session.setAttribute("message", "Password reset link has been sent, please check your email");
            } else {
                session.setAttribute("error", "Something went wrong, could not send email");
            }
        }
        return "redirect:/forgot";
    }

    @GetMapping("/reset")
    public String reset(@RequestParam String token, Model model) {

        if(token == null) {
            model.addAttribute("error", "Reset link is invalid or expired");
            return "message";
        }

        User existingUser =  userService.getUserByResetToken(token);

        if(ObjectUtils.isEmpty(existingUser)) {
            model.addAttribute("error", "Reset link is invalid or expired");
            return "message";
        }

        model.addAttribute("token", token);

        return "reset_password";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String password, @RequestParam String token, Model model) {

        User existingUser =  userService.getUserByResetToken(token);

        if(ObjectUtils.isEmpty(existingUser)) {
            model.addAttribute("error", "Reset link is invalid or expired");
            return "message";
        } else {

            userService.updateUserPassword(existingUser, password);

            model.addAttribute("message", "Password has been reset");

            return "message";
        }
    }

    @GetMapping("/models")
    public String models(Model model,
                         @RequestParam(value="category", defaultValue = "") String category,
                         @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                         @RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize) {

//        List<Asset> activeAssets = assetService.getAllActiveAssets(category);
//        model.addAttribute("assets", activeAssets);

        List<Category> activeCategories = categoryService.getAllActiveCategories();
        model.addAttribute("categories", activeCategories);
        model.addAttribute("activeCategory", category);

        Page<Asset> page = assetService.getAllActiveAssetsPagination(pageNumber, pageSize, category);
        List<Asset> assets = page.getContent();
        model.addAttribute("assets", assets);
        model.addAttribute("assetsSize", assets.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "search";
    }

    private User getLoggedUser(Principal p) {

        if(p != null) {
            String email = p.getName();
            User existingUser = userService.getUserByEmail(email);
            return existingUser;
        }
        
        return null;
    }

    @GetMapping("/models/{id}")
    public String modelPage(@PathVariable int id, Model model, Principal p) {

        Asset asset = assetService.getAssetById(id);

        User user = getLoggedUser(p);

        if(user != null) {
            List<AssetOrder> existingOrder = orderService.getOrderByUserIdAndAssetId(asset.getId(), user.getId());

            Boolean alreadyPurchased = false;

            if(!ObjectUtils.isEmpty(existingOrder)) {

                for(AssetOrder order : existingOrder) {
                    if(order.getStatus().equals("Fulfilled")) {
                        alreadyPurchased = true;
                    }
                }

            }

            if(alreadyPurchased) {
                model.addAttribute("alreadyPurchased", "true");
            } else {
                model.addAttribute("alreadyPurchased", "false");
            }
        } else {
            model.addAttribute("alreadyPurchased", "false");
        }

        model.addAttribute("asset", asset);

        return "view_model";
    }

    @GetMapping("/models/search")
    public String search(@RequestParam String query, Model model,
                         @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                         @RequestParam(name = "pageSize", defaultValue = "24") Integer pageSize) {

        List<Category> activeCategories = categoryService.getAllActiveCategories();
        model.addAttribute("categories", activeCategories);

        Page<Asset> page = null;
        if(query != null && query.length() > 0) {
            page = assetService.searchAssetActivePagination(query, pageNumber, pageSize);
        } else {
            page = assetService.searchAllAssetsActivePagination(pageNumber, pageSize);
        }

        List<Asset> assets = page.getContent();
        model.addAttribute("assets", assets);
        model.addAttribute("assetsSize", assets.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "search";
    }

}
