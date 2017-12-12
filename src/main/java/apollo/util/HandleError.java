package apollo.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to tag methods
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since 25-May-2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleError {
	// Marker Annotation
}
