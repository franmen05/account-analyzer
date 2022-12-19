package com.guille.reposiitory;

import com.guille.domain.Deduction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeductionRepository implements PanacheRepository<Deduction> {
}
