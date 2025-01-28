package com.example.blog.service;

import com.example.blog.model.User;
import com.example.blog.model.UserProfile;
import com.example.blog.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Value("${app.upload.dir.profile-images}")
    private String uploadDir;

    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository){
        this.userProfileRepository = userProfileRepository;
    }

    public Optional<UserProfile> findByUser(User user){
        return userProfileRepository.findByUser(user);
    }

    public Optional<UserProfile> findByUserEmail(String email){
        return userProfileRepository.findByUserEmail(email);
    }

    public UserProfile createUserProfile(User user, String displayName) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName(displayName)
                .bio("")
                .isPublic(true)
                .updatedAt(LocalDateTime.now())
                .build();
        return userProfileRepository.save(profile);
    }

    public UserProfile updateUserProfile(UserProfile profile){
        profile.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(profile);
    }

    /**
     * Handles profile image upload and updates the profileImagePath.
     *
     * @param profile      The UserProfile to update.
     * @param profileImage The uploaded MultipartFile for the profile image.
     * @return The updated UserProfile.
     * @throws IOException If an error occurs during file handling.
     */
    public UserProfile updateUserProfile(UserProfile profile, MultipartFile profileImage) throws IOException {
        if (profileImage != null && !profileImage.isEmpty()) {
            File uploadFolder = Paths.get(uploadDir).toFile();
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String originalFilename = profileImage.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            File destinationFile = Paths.get(uploadDir, uniqueFilename).toFile();
            profileImage.transferTo(destinationFile);

            if (profile.getProfileImagePath() != null) {
                String oldImageName = profile.getProfileImagePath().replace("/profile-images/", "");
                File oldImage = Paths.get(uploadDir, oldImageName).toFile();
                if (oldImage.exists()) {
                    oldImage.delete();
                }
            }

            profile.setProfileImagePath("/profile-images/" + uniqueFilename);
        }

        profile.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(profile);
    }
}
