package com.virnect.platform.workspace;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestTraceEnvironmentPostProcessor implements EnvironmentPostProcessor {
	private static final String PROPERTY_SOURCE_NAME = "defaultProperties";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> map = new HashMap<String, Object>();
		// This doesn't work with all logging systems but it's a useful default so you see
		// traces in logs without having to configure it.
		String hostIP = "-";

		if (System.getenv("eureka.instance.ip-address") != null) {
			hostIP = System.getenv("eureka.instance.ip-address");
		}
		if (Boolean
			.parseBoolean(environment.getProperty("spring.sleuth.enabled", "true"))) {
			map.put("logging.pattern.level", "%5p [" + hostIP + "] [${spring.zipkin.service.name:"
				+ "${spring.application.name:}},%X{traceId:-},%X{spanId:-}]");
		} else {
			map.put("logging.pattern.level", "%5p [" + hostIP + "] [${spring.application.name:}},%X{traceId:-},%X{spanId:-}]");
		}
		addOrReplace(environment.getPropertySources(), map);
	}

	private void addOrReplace(
		MutablePropertySources propertySources,
		Map<String, Object> map
	) {
		MapPropertySource target = null;
		if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
			PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
			if (source instanceof MapPropertySource) {
				target = (MapPropertySource)source;
				for (String key : map.keySet()) {
					if (!target.containsProperty(key)) {
						target.getSource().put(key, map.get(key));
					}
				}
			}
		}
		if (target == null) {
			target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
		}
		if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
			propertySources.addLast(target);
		}
	}
}
