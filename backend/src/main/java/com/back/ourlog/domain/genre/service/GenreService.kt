package com.back.ourlog.domain.genre.service;

import com.back.ourlog.domain.genre.entity.Genre;
import com.back.ourlog.domain.genre.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getGenresByIds(List<Integer> ids) {
        return genreRepository.findAllById(ids);
    }

    public Genre findOrCreateByName(String name) {
        return genreRepository.findByName(name)
                .orElseGet(() -> genreRepository.save(new Genre(name)));
    }
}
