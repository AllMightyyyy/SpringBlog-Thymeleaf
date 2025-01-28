package com.example.blog.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ThemeController {

    @GetMapping("/theme")
    public String setTheme(@RequestParam("theme") String theme, HttpServletRequest request, HttpServletResponse response) {
        // Validate the theme parameter
        if (!theme.equals("dark") && !theme.equals("light") &&
                !theme.equals("material") && !theme.equals("material-space")) {
            theme = "dark"; // default theme
        }

        // Create a cookie to store the theme preference, valid for 30 days
        Cookie themeCookie = new Cookie("theme", theme);
        themeCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        themeCookie.setPath("/");
        response.addCookie(themeCookie);

        // Redirect back to the referring page or home if not available
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty() && referer.startsWith(request.getScheme() + "://" + request.getServerName())) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
