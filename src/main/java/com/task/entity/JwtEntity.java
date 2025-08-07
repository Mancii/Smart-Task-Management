package com.task.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "JWT_TOKENS")
@Data
public class JwtEntity implements Serializable {

	private static final long serialVersionUID = 7509357362771157844L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TOKEN_ID_SEQ_GEN")
	@SequenceGenerator(name = "TOKEN_ID_SEQ_GEN", sequenceName = "TOKEN_ID_SEQ_GEN", allocationSize = 1)
	@Column(name = "TOKEN_ID")
	private Long tokenId;

	@Column(name = "REFRESH_TOKEN")
	private String refreshToken;

	@Column(name = "ACCESS_TOKEN")
	private String accessToken;

	@Column(name = "VALID")
	private Long validId;
	
	@Column(name = "USER_ID")
	private Long userId;

	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Instant createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp
	@Column(name = "UPDATED_AT", nullable = false)
	private Instant updatedAt;

}
