package com.example.breeze_seas;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Minimal local non-null annotation used to satisfy unqualified references in shared code.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE
})
public @interface NonNull {
}
