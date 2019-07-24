package ru.abagiev.examples.spring.intercept.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Component
@Slf4j
public class BeanProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (clazz.isAnnotationPresent(RestController.class)) {
            log.info("Registering REST: " + clazz.getSimpleName());

            Enhancer en = new Enhancer();
            en.setSuperclass(clazz);
            en.setCallback(new RestControllerProxy(bean, clazz));
            Class<?>[] paramTypes = clazz.getConstructors()[0].getParameterTypes();
            return en.create(paramTypes, new Object[paramTypes.length]);
        }

        return bean;
    }
}
