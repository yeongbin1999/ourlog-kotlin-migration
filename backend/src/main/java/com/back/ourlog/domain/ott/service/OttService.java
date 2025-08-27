package com.back.ourlog.domain.ott.service;

import com.back.ourlog.domain.ott.entity.Ott;
import com.back.ourlog.domain.ott.repository.OttRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OttService {
    private final OttRepository ottRepository;

    public List<Ott> getOttsByIds(List<Integer> ids) {
        return ottRepository.findAllById(ids);
    }
}
