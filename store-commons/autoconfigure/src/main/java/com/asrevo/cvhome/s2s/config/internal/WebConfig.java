package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@Configuration
@ConditionalOnClass(Pageable.class)
@Import(value = {ReactiveWebConfig.class, ServletWebConfig.class})
public class WebConfig {}
