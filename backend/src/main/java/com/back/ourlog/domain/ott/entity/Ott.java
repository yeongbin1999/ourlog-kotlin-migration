package com.back.ourlog.domain.ott.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ott {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String logoUrl;

    @OneToMany(mappedBy = "ott", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryOtt> diaryOtts = new ArrayList<>();

    public Ott(String name) {
        this.name = name;
    }

    public Ott(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

}
