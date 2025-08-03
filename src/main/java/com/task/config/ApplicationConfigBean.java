package com.task.config;

import com.task.entity.AppConfig;
import com.task.entity.AppConfigParam;
import com.task.repo.AppConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Scope("singleton")
public class ApplicationConfigBean {

	public static Map<Long, AppConfig> configDetailsMap;

	private final AppConfigRepository appConfigRepository;

	@PostConstruct
	public void load() {
		List<AppConfig> configDetailsList = appConfigRepository.findAll();

		convertListToMap(configDetailsList);
	}

	private void convertListToMap(List<AppConfig> configDetailsList) {
		Map<String, AppConfigParam> paramsMap = null;
		configDetailsMap = new ConcurrentHashMap<>();
		if (configDetailsList != null && configDetailsList.size() > 0) {
			for (AppConfig conf : configDetailsList) {
				if (conf.getAppConfigParams() != null && !conf.getAppConfigParams().isEmpty()) {
					paramsMap = new ConcurrentHashMap<>();
					for (AppConfigParam param : conf.getAppConfigParams()) {
						paramsMap.put(param.getKey(), param);
						conf.setParamsMap(paramsMap);
					}
				}
				if (!configDetailsMap.containsKey(conf.getId())) {
					configDetailsMap.put(conf.getId(), conf);
				}
			}
		}
	}

	public void reload() {
		load();
	}
}
