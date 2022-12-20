package com.guille.domain;

import javax.persistence.*;

@Entity
public class Deduction{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DeductionType type;

    private String description;

    public Deduction() {
    }

    public static Deduction  build(DeductionType type, String description) {
        var d= new Deduction();
        d.type = type;
        d.description = description;
        return d;
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
