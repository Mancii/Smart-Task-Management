package com.task.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "APP_CONFIG")
public class AppConfig implements Serializable {

	@Serial
	private static final long serialVersionUID = -4823312941476731164L;

	@Id
	private Long id;

	@Column(name = "DESCRIPTION")
	private String description;

	@OneToMany(mappedBy = "appConfig", fetch = FetchType.EAGER)
	private List<AppConfigParam> appConfigParams;

	@Transient
	private Map<String, AppConfigParam> paramsMap;
}