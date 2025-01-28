package com.example.blog.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    private User user;

    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profileImagePath;

    private Boolean isPublic = true;

    private LocalDateTime updatedAt;

    private String twitterUrl;
    private String facebookUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String instagramUrl;

    /**
     * Checks if any of the social URLs are present and have text.
     *
     * @return true if at least one social URL is non-null and not empty; false otherwise.
     */
    public boolean hasAnySocialUrl() {
        return (twitterUrl != null && !twitterUrl.trim().isEmpty()) ||
                (facebookUrl != null && !facebookUrl.trim().isEmpty()) ||
                (linkedinUrl != null && !linkedinUrl.trim().isEmpty()) ||
                (githubUrl != null && !githubUrl.trim().isEmpty()) ||
                (instagramUrl != null && !instagramUrl.trim().isEmpty());
    }
}
