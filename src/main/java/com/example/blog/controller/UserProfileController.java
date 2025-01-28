package com.example.blog.controller;

import com.example.blog.model.User;
import com.example.blog.model.UserProfile;
import com.example.blog.service.UserProfileService;
import com.example.blog.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService, UserService userService){
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    /**
     * View own profile
     */
    @GetMapping("/me")
    public String viewOwnProfile(Authentication authentication, Model model){
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile profile = userProfileService.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        model.addAttribute("profile", profile);
        return "view_own_profile";
    }

    /**
     * Edit own profile
     */
    @GetMapping("/me/edit")
    public String editOwnProfile(Authentication authentication, Model model){
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile profile = userProfileService.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        model.addAttribute("profile", profile);
        return "edit_own_profile";
    }

    @PostMapping("/me/edit")
    public String updateOwnProfile(@Valid @ModelAttribute("profile") UserProfile profile,
                                   BindingResult result,
                                   @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                   @RequestParam(value = "twitterUrl", required = false) String twitterUrl,
                                   @RequestParam(value = "facebookUrl", required = false) String facebookUrl,
                                   @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
                                   @RequestParam(value = "githubUrl", required = false) String githubUrl,
                                   @RequestParam(value = "instagramUrl", required = false) String instagramUrl,
                                   @RequestParam(value = "bioHtmlContent", required = false) String bioHtmlContent,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            return "edit_own_profile";
        }

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile existingProfile = userProfileService.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        existingProfile.setDisplayName(profile.getDisplayName());

        String sanitizedBio = sanitizeHtml(bioHtmlContent);
        existingProfile.setBio(sanitizedBio);

        existingProfile.setIsPublic(profile.getIsPublic());

        existingProfile.setTwitterUrl(twitterUrl);
        existingProfile.setFacebookUrl(facebookUrl);
        existingProfile.setLinkedinUrl(linkedinUrl);
        existingProfile.setGithubUrl(githubUrl);
        existingProfile.setInstagramUrl(instagramUrl);

        try {
            userProfileService.updateUserProfile(existingProfile, profileImage);
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to upload profile image.");
            return "redirect:/profiles/me/edit";
        }

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profiles/me";
    }

    /**
     * View other users' profiles
     */
    @GetMapping("/{userId}")
    public String viewUserProfile(@PathVariable Long userId,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes){
        User user = userService.findById(userId).orElse(null);
        if(user == null){
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/";
        }

        UserProfile profile = userProfileService.findByUser(user).orElse(null);
        if(profile == null){
            redirectAttributes.addFlashAttribute("error", "Profile not found.");
            return "redirect:/";
        }

        if(!profile.getIsPublic()){
            if(authentication == null || !authentication.getName().equals(profile.getUser().getEmail())){
                redirectAttributes.addFlashAttribute("error", "This profile is private.");
                return "redirect:/";
            }
        }

        model.addAttribute("profile", profile);
        return "view_user_profile";
    }

    /**
     * Helper method to sanitize HTML using JSoup
     */
    private String sanitizeHtml(String htmlContent){
        if(htmlContent == null){
            return "";
        }
        Safelist safelist = Safelist.basicWithImages()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                .addAttributes("img", "src", "alt", "title")
                .addAttributes("a", "href", "title", "target")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https");

        return Jsoup.clean(htmlContent, safelist);
    }
}
