package com.maotuanzi.service;

import com.maotuanzi.dto.PetCreateRequest;
import com.maotuanzi.entity.Pet;

import java.util.List;

public interface PetService {
    List<Pet> list();
    Pet detail(Long id);
    Pet create(PetCreateRequest req);
    Pet update(Long id, PetCreateRequest req);
    void delete(Long id);
}