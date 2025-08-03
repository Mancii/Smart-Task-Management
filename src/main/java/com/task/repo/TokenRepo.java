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
	JwtEntity findByRefreshToken(String refreshToken);
	 
	@Modifying
	@Transactional
	@Query("update JwtEntity j set j.validId = 0, j.updatedAt = :currentDate where j.accessToken = :token")
	Integer invalidateToken(@Param("token")String token, @Param("currentDate")Date currentDate);

	@Modifying
	@Transactional
	@Query("update JwtEntity j set j.validId = 0, j.updatedAt = :currentDate where j.tokenId = :tokenId")
	Integer invalidateTokenById(@Param("tokenId") Long tokenId, @Param("currentDate") Date currentDate);

}
