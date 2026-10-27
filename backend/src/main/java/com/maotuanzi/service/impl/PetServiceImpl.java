package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.dto.PetCreateRequest;
import com.maotuanzi.entity.Pet;
import com.maotuanzi.entity.User;
import com.maotuanzi.mapper.PetMapper;
import com.maotuanzi.mapper.SystemConfigMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.PetService;
import com.maotuanzi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final SystemConfigMapper configMapper;
    private final UserService userService;

    @Override
    public List<Pet> list() {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return petMapper.selectByUserId(uid);
    }

    @Override
    public Pet detail(Long id) {
        Long uid = UserContext.getUserId();
        Pet p = petMapper.selectById(id);
        if (p == null || !p.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.PET_NOT_FOUND);
        }
        return p;
    }

    @Override
    @Transactional
    public Pet create(PetCreateRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);

        Long count = petMapper.countByUserId(uid);

        long limit = 2;
        try {
            var conf = configMapper.selectByKey("quota.free.pet_count");
            if (conf != null) limit = Long.parseLong(conf.getConfigValue());
        } catch (Exception ignore) {}

        boolean isVip = userService.isVip(uid);

        if (!isVip && count >= limit) {
            throw BizException.of(ErrorCode.PET_LIMIT_EXCEEDED);
        }

        Pet p = new Pet();
        p.setUserId(uid);
        p.setNickname(req.getNickname());
        p.setSpecies(req.getSpecies());
        p.setBreedId(req.getBreedId());
        p.setBreedName(req.getBreedName());
        p.setGender(req.getGender());
        p.setBirthday(req.getBirthday());
        p.setWeightKg(req.getWeightKg());
        p.setIsNeutered(req.getIsNeutered() != null ? req.getIsNeutered() : 0);
        p.setAllergies(req.getAllergies());
        p.setStapleFood(req.getStapleFood());
        p.setAvatarUrl(req.getAvatarUrl());
        p.setRemark(req.getRemark());
        petMapper.insert(p);
        return p;
    }

    @Override
    @Transactional
    public Pet update(Long id, PetCreateRequest req) {
        Long uid = UserContext.getUserId();
        Pet exist = petMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.PET_NOT_FOUND);
        }
        Pet p = new Pet();
        p.setId(id);
        p.setNickname(req.getNickname());
        p.setSpecies(req.getSpecies());
        p.setBreedId(req.getBreedId());
        p.setBreedName(req.getBreedName());
        p.setGender(req.getGender());
        p.setBirthday(req.getBirthday());
        p.setWeightKg(req.getWeightKg());
        p.setIsNeutered(req.getIsNeutered());
        p.setAllergies(req.getAllergies());
        p.setStapleFood(req.getStapleFood());
        p.setAvatarUrl(req.getAvatarUrl());
        p.setRemark(req.getRemark());
        petMapper.updateById(p);
        return petMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long uid = UserContext.getUserId();
        Pet exist = petMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.PET_NOT_FOUND);
        }
        petMapper.softDelete(id, LocalDateTime.now());
    }
}