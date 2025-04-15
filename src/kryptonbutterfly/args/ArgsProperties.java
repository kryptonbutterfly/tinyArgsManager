package kryptonbutterfly.args;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface ArgsProperties
{
	/**
	 * @return the identifiers that trigger the printing of the help message.
	 */
	public String[] helpIDs() default { "h", "-help" };
	
	/**
	 * @return whether to terminate the program after printing the help or continue
	 *         execution.
	 */
	public boolean terminateAfterHelp() default true;
	
	/**
	 * @return The prefix to be expected in the beginning of an argument identifier.
	 */
	public String idPrefix() default "-";
	
}