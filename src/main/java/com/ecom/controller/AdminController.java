package com.ecom.controller;

import com.ecom.model.Asset;
import com.ecom.model.AssetOrder;
import com.ecom.model.Category;
import com.ecom.model.User;
import com.ecom.service.*;
import com.ecom.util.CommonUtil;
import com.ecom.util.IndianStatesMap;
import com.ecom.util.OrderStatus;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

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
    public String index() {
        return "admin/index";
    }

    // Model/Asset

    @GetMapping("/add_model")
    public String add_model(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/add_model";
    }

    @PostMapping("/add_model")
    public String saveModel(@Valid @ModelAttribute Asset asset, BindingResult result, @RequestParam("file1") MultipartFile imageName, @RequestParam("file2") MultipartFile fileName, HttpSession session) throws IOException {

        Asset saveAsset = assetService.addAsset(asset, imageName, fileName);

        if(!ObjectUtils.isEmpty(saveAsset)) {
            session.setAttribute("message", "Model has been added successfully");
        } else {
            session.setAttribute("error", "Model has not been added");
        }

        return "redirect:/admin/add_model";
    }

    @GetMapping("/view_models")
    public String view_models(Model model, @RequestParam(defaultValue = "") String query,
                              @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//        if(query != null && query.length() > 0) {
//            List<Asset> assets = assetService.searchAsset(query);
//            model.addAttribute("assets", assets);
//        } else {
//            model.addAttribute("assets", assetService.getAllAssets());
//        }

        Page<Asset> page = null;
        if(query != null && query.length() > 0) {
            page = assetService.searchAssetPagination(pageNumber, pageSize, query);

        } else {
            page = assetService.getAllAssetsPagination(pageNumber, pageSize);
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

        return "admin/view_models";
    }

    @GetMapping("/edit_model/{id}")
    public String edit_model(@PathVariable int id, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("assets", assetService.getAssetById(id));
        return "admin/edit_model";
    }

    @PostMapping("/updateModel")
    public String updateModel(@Valid @ModelAttribute Asset asset, @RequestParam("file1") MultipartFile imageName, @RequestParam("file2") MultipartFile fileName, HttpSession session) throws IOException {
        Asset newAsset = assetService.updateAsset(asset, imageName, fileName);

        if(!ObjectUtils.isEmpty(newAsset)) {
            session.setAttribute("message", "Model has been updated successfully");
        } else {
            session.setAttribute("error", "Model has not been updates");
        }
        return "redirect:/admin/edit_model/" + asset.getId();
    }

    @GetMapping("/delete_model/{id}")
    public String delete_model(@PathVariable int id, HttpSession session) {
        Boolean status = assetService.deleteAsset(id);

        if (status) {
            session.setAttribute("message", "Model has been deleted successfully");
        } else {
            session.setAttribute("error", "Model has not been deleted");
        }

        return "redirect:/admin/view_models";
    }

    // Category

    @GetMapping("/add_category")
    public String add_category(Model model,
                               @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Page<Category> page = categoryService.getAllActiveCategoriesPagination(pageNumber, pageSize);
        List<Category> categories = page.getContent();
        model.addAttribute("categories", categories);
        model.addAttribute("categoriesSize", categories.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

//        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/add_category";
    }

    @PostMapping("/save_category")
    public String saveCategory(@Valid @ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        Category saveCategory = categoryService.addCategory(category, file);

        if(!ObjectUtils.isEmpty(saveCategory)) {
            session.setAttribute("message", "Category saved successfully");
        } else {
            session.setAttribute("error", "Category already exists");
        }
        return "redirect:/admin/add_category";
    }

    @GetMapping ("/delete_category/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {
        Boolean status = categoryService.deleteCategory(id);
        if(status) {
            session.setAttribute("message", "Category deleted successfully");
        } else {
            session.setAttribute("error", "Category could not be deleted");
        }
        return "redirect:/admin/add_category";
    }

    @GetMapping("/edit_category/{id}")
    public String edit_category(@PathVariable int id, Model model) {
        model.addAttribute("category", categoryService.findCategoryById(id));
        return "admin/edit_category";

    }

    @PostMapping("/updateCategory")
    public String updateCategory(@Valid @ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

        Category newCategory = categoryService.updateCategory(category, file);

        if(!ObjectUtils.isEmpty(newCategory)) {
            session.setAttribute("message", "Category updated successfully");
        } else {
            session.setAttribute("error", "Category could not be updated");
        }

        return "redirect:/admin/edit_category/" + category.getId();
    }

    // Users

    @GetMapping("/users")
    public String users(Model model,
                        @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
//        List<User> users = userService.getUsersByRole("ROLE_USER");
//        model.addAttribute("users", users);

        Page<User> page = userService.getUsersByRolePagination("ROLE_USER", pageNumber, pageSize);

        List<User> users = page.getContent();
        model.addAttribute("users", users);
        model.addAttribute("usersSize", users.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/users";
    }

    @GetMapping("/user/update_status")
    public String updateUserAccountStatus(Principal p, @RequestParam Integer id, @RequestParam Boolean status, HttpSession session) {

        User user = getLoggedUser(p);

        if(user.getId() == id) {
            session.setAttribute("error", "Could not update user account status");
            return "redirect:/admin/users";
        }
        Boolean condition = userService.updateAccountStatus(id, status);

        if(condition) {
            session.setAttribute("message", "Status updated successfully");
        } else {
            session.setAttribute("error", "Status could not be updated");
        }

        return "redirect:/admin/users";
    }

    // Orders

    @GetMapping("/orders")
    public String orders(Model model, @RequestParam(defaultValue = "") String query,
                         @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//        if(query != null && query.length() > 0) {
//            List<AssetOrder> orders = orderService.getOrderBySearch(query);
//            model.addAttribute("orders", orders);
//        } else {
//            List<AssetOrder> orders = orderService.getAllOrders();
//            model.addAttribute("orders", orders);
//        }


        Page<AssetOrder> page = null;
        if(query != null && query.length() > 0) {
            page = orderService.getOrderBySearchPagination(query, pageNumber, pageSize);
        } else {
            page = orderService.getAllOrdersPagination(pageNumber, pageSize);
        }

        List<AssetOrder> orders = page.getContent();
        model.addAttribute("orders", orders);
        model.addAttribute("ordersSize", orders.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/adminOrderPanel";
    }

    @PostMapping("/update-order-status")
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

        return "redirect:/admin/orders";
    }

    // Admin

    private User getLoggedUser(Principal p) {

        String email = p.getName();
        User user = userService.getUserByEmail(email);

        return user;
    }

    @GetMapping("/add-admin")
    public String adminPage(Principal p, Model model,
                            @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        User user = getLoggedUser(p);
        model.addAttribute("currentAdminEmail", user.getEmail());

        Page<User> page = userService.getUsersByRolesPagination("ROLE_ADMIN", pageNumber, pageSize);

        List<User> users = page.getContent();
        model.addAttribute("users", users);
        model.addAttribute("usersSize", users.size());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/admins";

    }

    @GetMapping("/add-admin/register-admin")
    public String registerAdminPage(Model model) {
        LinkedHashMap<String, String> states = IndianStatesMap.getStatesLinkedHashMap();
        model.addAttribute("states", states);
        return "admin/register_admin";
    }

    @PostMapping("/add-admin/register-admin")
    public String registerAdmin(@Valid @ModelAttribute("newUser") User newUser, BindingResult result, HttpSession session) {

        if(result.hasErrors()){
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            session.setAttribute("error", errorMessage);
            return "redirect:/admin/add-admin/register-admin";
        }

        User savedUser = userService.saveAdmin(newUser);

        if(!ObjectUtils.isEmpty(savedUser)) {
            session.setAttribute("message", "Admin has been created successfully");
        } else {
            session.setAttribute("error", "Email already exists");
        }

        return "redirect:/admin/add-admin/register-admin";
    }

    @GetMapping("/add-admin/update_status")
    public String updateAdminAccountStatus(Principal p, @RequestParam Integer id, @RequestParam Boolean status, HttpSession session) {

        User user = getLoggedUser(p);

        if(user.getId() == id) {
            session.setAttribute("error", "Could not update user account status");
            return "redirect:/admin/add-admin";
        }

        Boolean condition = userService.updateAccountStatus(id, status);

        if(condition) {
            session.setAttribute("message", "Status updated successfully");
        } else {
            session.setAttribute("error", "Status could not be updated");
        }

        return "redirect:/admin/add-admin";
    }

}
