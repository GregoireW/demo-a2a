package com.ownorg.ai.a2a.definition;


import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Agent {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
