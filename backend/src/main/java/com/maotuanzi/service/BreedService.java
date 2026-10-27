package com.maotuanzi.service;

import com.maotuanzi.entity.Breed;

import java.util.List;

public interface BreedService {
    List<Breed> list(Integer species);
    Breed detail(Integer id);
}