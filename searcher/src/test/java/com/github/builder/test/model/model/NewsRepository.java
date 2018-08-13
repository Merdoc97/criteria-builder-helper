package com.github.builder.test.model.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**

 */
@Repository
public interface NewsRepository extends JpaRepository<NewsEntity,Integer>,JpaSpecificationExecutor<NewsEntity> {
}
