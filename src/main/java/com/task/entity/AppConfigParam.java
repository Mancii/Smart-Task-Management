package com.task.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "APP_CONFIG_PARAM")
public class AppConfigParam implements Serializable {

	@Serial
	private static final long serialVersionUID = -5067754344035888470L;

	@Id
	private Long id;
	private String description;
	private String key;
	private String value;

	@ManyToOne
	@JoinColumn(name = "CONFIG_ID")
	private AppConfig appConfig;

}
