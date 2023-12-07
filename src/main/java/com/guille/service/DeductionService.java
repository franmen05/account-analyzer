package com.guille.service;

import com.guille.domain.Deduction;
import com.guille.reposiitory.DeductionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

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

    public List<Deduction> listAll(){
        return deductionRepository.listAll();
    }
    @Transactional
    public Boolean delete(Long id){
        return deductionRepository.deleteById(id);
    }
}
