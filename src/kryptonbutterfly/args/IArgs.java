package kryptonbutterfly.args;

import java.util.Arrays;

import kryptonbutterfly.monads.opt.Opt;

@ArgsProperties
public interface IArgs
{
	/**
	 * @return A description of the program.
	 */
	public String programInfo();
	
	public default void printHelp(ArgsParser parser)
	{
		System.out.printf("%s\n\n", this.programInfo());
		
		final var prop = Opt.of(this.getClass().getAnnotation(ArgsProperties.class))
			.get(() -> IArgs.class.getAnnotation(ArgsProperties.class));
		
		final var ids = Arrays.stream(prop.helpIDs())
			.map(id -> prop.idPrefix() + id)
			.reduce("%s %s"::formatted)
			.orElse("");
		
		if (prop.terminateAfterHelp())
			System.out.printf(" %-16s  display this help and exit.\n\n", ids);
		else
			System.out.printf(" %-16s  display this help.\n\n", ids);
		
		for (final var field : this.getClass().getDeclaredFields())
			Opt.of(field.getAnnotation(Argument.class))
				.if_(arg -> System.out.printf(" %-16s  %s\n", prop.idPrefix() + arg.name(), arg.info()));
		
		for (final var method : this.getClass().getDeclaredMethods())
			Opt.of(method.getAnnotation(Argument.class))
				.if_(arg -> System.out.printf(" %-16s  %s\n", prop.idPrefix() + arg.name(), arg.info()));
		
		if (prop.terminateAfterHelp())
			parser.terminateAction.accept(0);
		
		System.out.println();
	}
}
