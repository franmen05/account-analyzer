package com.guille.service;

import com.guille.domain.Deduction;
import com.guille.reposiitory.DeductionRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class DeductionService {

    private final DeductionRepository deductionRepository;

    public DeductionService(DeductionRepository deductionRepository) {
        this.deductionRepository=deductionRepository;
    }

    @Transactional
    public void create(Deduction deduction){
        deductionRepository.persist(deduction);
    }
}
