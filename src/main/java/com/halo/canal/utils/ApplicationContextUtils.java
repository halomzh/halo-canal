package com.halo.canal.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author shoufeng
 */

@Component
public class ApplicationContextUtils implements ApplicationContextAware {

	public static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ApplicationContextUtils.applicationContext = applicationContext;
	}

	public static <T> T getBean(Class<T> tClass) {
		return applicationContext.getBean(tClass);
	}

	public static <T> T getBean(Class<T> tClass, String beanName) {
		return applicationContext.getBean(beanName, tClass);
	}

	public static void publishEvent(Object event) {
		applicationContext.publishEvent(event);
	}

}
