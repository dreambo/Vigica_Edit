package vigica.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BeanFactory implements ApplicationContextAware {

	private static ApplicationContext context;

	public ApplicationContext getContext() {
		return context;
	}

	public static IService getService() {
		return getBean(Service_BDD.class);
		// return new Service_BDD2();
	}

	public static <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		context = ctx;
	}
}
