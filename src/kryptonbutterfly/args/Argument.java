package kryptonbutterfly.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Argument
{
	/**
	 * @return The Argument name.
	 */
	String name();
	
	/**
	 * @return The arguments description.
	 */
	String info();
	
	/**
	 * @return All the arguments that are required if this argument is being
	 *         supplied.
	 */
	String[] requires() default {};
	
	/**
	 * @return All the arguments that must not be supplied if this one is.
	 */
	String[] excludes() default {};
	
	/**
	 * @return Whether this argument is always required.
	 */
	boolean isRequired() default false;
}