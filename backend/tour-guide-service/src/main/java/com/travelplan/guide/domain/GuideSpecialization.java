package com.travelplan.guide.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "guide_specializations")
public class GuideSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Specialization name is required")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    @JsonIgnore
    private Guide guide;
    
    public GuideSpecialization() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) { this.guide = guide; }
}
