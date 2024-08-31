package io.pality.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Replace.List.class)
public @interface Replace {
    String value();

    Architecture arch() default Architecture.ANY;

    OperatingSystem os() default OperatingSystem.ANY;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {
        Replace[] value();

    }

}
