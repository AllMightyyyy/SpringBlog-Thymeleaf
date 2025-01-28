package com.example.blog.controller;

import com.example.blog.model.User;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class BaseController {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addUserAttributes(Authentication authentication, Model model){
        if(authentication != null && authentication.isAuthenticated()){
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if(user != null && user.getUserProfile() != null){
                model.addAttribute("displayName", user.getUserProfile().getDisplayName());
            } else {
                model.addAttribute("displayName", "Anonymous");
            }
        } else {
            model.addAttribute("displayName", "Anonymous");
        }
    }
}
