package fr.yurictf.extentions.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public @Retention(RetentionPolicy.RUNTIME) @interface CTF_EVENT_REGISTERER {
    String author() default "Yuri6037";
    String date() default "10/04/2014";
    int revision() default 1;
    String comments() default "Used by event registerer system";
}
