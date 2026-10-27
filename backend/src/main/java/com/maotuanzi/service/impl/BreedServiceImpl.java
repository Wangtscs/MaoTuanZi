package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.entity.Breed;
import com.maotuanzi.mapper.BreedMapper;
import com.maotuanzi.service.BreedService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BreedServiceImpl implements BreedService {

    private final BreedMapper breedMapper;

    @Override
    @Cacheable(value = "breeds", key = "#species")
    public List<Breed> list(Integer species) {
        if (species == null) {
            return breedMapper.selectAll(null, null, 0, 1000);
        }
        return breedMapper.selectBySpecies(species);
    }

    @Override
    public Breed detail(Integer id) {
        Breed b = breedMapper.selectById(id);
        if (b == null) throw BizException.of(ErrorCode.NOT_FOUND);
        return b;
    }
}