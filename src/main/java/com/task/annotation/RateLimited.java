package com.task.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    String value() default "";
    int requests() default 100;  // Default number of requests
    int duration() default 60;   // Duration in seconds
}
