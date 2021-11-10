package de.tinycodecrank.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Argument
{
	static boolean requiresMinusAtIdentifier = true;
	
	String name();
	
	String info();
	
	String[] requires() default {};
	
	String[] excludes() default {};
	
	boolean isRequired() default false;
}