package com.task.repo;

import com.task.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Integer> {

  @Query("select c from AppConfig c join fetch c.appConfigParams where c.id = :id")
  Optional<AppConfig> findConfigById(Long id);

}
