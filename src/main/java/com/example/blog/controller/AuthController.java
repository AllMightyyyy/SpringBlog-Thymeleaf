package com.example.blog.controller;

import com.example.blog.model.User;
import com.example.blog.service.UserService;
import com.example.blog.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @Autowired
    public AuthController(UserService userService, UserProfileService userProfileService){
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            return "register";
        }
        if(userService.findByEmail(user.getEmail()).isPresent()){
            model.addAttribute("error", "Email already registered");
            return "register";
        }
        userService.registerUser(user);
        userProfileService.createUserProfile(user, user.getName());
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(){
        return "login";
    }
}
