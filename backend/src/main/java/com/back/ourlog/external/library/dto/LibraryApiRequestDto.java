package com.back.ourlog.external.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LibraryApiRequestDto {
    @NotBlank
    private String title;

    public LibraryApiRequestDto(String title) {
        this.title = title;
    }
}
