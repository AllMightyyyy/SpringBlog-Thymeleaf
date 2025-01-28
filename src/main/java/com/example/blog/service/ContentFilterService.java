package com.example.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentFilterService {

    private final List<String> forbiddenKeywords;

    @Autowired
    public ContentFilterService(@Value("#{'${content.filter.forbidden-keywords}'.split(',')}") List<String> forbiddenKeywords){
        this.forbiddenKeywords = forbiddenKeywords;
    }

    /**
     * Checks if the content is safe based on forbidden keywords.
     *
     * @param content The content to check.
     * @return True if content is safe, false otherwise.
     */
    public boolean isContentSafe(String content) {
        if (content == null) return true;
        String lowerContent = content.toLowerCase();
        return forbiddenKeywords.stream().noneMatch(lowerContent::contains);
    }

    /**
     * Retrieves the list of forbidden keywords.
     *
     * @return List of forbidden keywords.
     */
    public List<String> getForbiddenKeywords() {
        return forbiddenKeywords;
    }
}
