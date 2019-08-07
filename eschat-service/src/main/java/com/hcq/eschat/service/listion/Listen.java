package com.hcq.eschat.service.listion;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Listen implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Map<String, Init> beansOfType = contextRefreshedEvent.getApplicationContext().getBeansOfType(Init.class);
        beansOfType.forEach((k, v) -> {
            v.init();
        });
    }
}
