package com.travelplan.guide.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "guide_languages")
public class GuideLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Language name is required")
    private String language;

    @NotBlank(message = "Proficiency level is required")
    private String level; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    @JsonIgnore
    private Guide guide;

    public GuideLanguage() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) { this.guide = guide; }
}
