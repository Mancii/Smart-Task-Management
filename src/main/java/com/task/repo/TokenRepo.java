package com.task.repo;

import com.task.entity.JwtEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface TokenRepo extends JpaRepository<JwtEntity, Long>{

	JwtEntity findByUserId(Long userId);
	 
}
