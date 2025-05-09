package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.repository.InformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class InformationService {

    private final InformationRepository informationRepository;

    public List<Information> getAllInformation() {
        return informationRepository.findAll();
    }

}
