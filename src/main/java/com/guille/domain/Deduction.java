package com.guille.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
public class Deduction{
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private DeductionType type;

    private String description;

    public Deduction() {
    }

    public DeductionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
