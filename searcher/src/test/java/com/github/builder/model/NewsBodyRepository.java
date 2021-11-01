package com.github.builder.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsBodyRepository extends JpaRepository<NewsBodyEntity,String>,JpaSpecificationExecutor<NewsBodyEntity> {

}
