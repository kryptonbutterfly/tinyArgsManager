package kryptonbutterfly.args.internal;

import static kryptonbutterfly.math.utils.range.Range.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import kryptonbutterfly.args.ArgsProperties;
import kryptonbutterfly.args.Argument;
import kryptonbutterfly.args.IArgs;
import kryptonbutterfly.args.TypeParser;

public final class SanityChecker<Args extends IArgs>
{
	private final Args								target;
	private final String							idPrefix;
	private final HashMap<Class<?>, TypeParser<?>>	typeParser;
	
	private final ArrayList<String>		issues		= new ArrayList<>();
	private final ArrayList<Argument>	required	= new ArrayList<>();
	private final ArrayList<Argument>	allArgs		= new ArrayList<>();
	
	private SanityChecker(HashMap<Class<?>, TypeParser<?>> typeParser, String idPrefix, Args target)
	{
		this.typeParser	= typeParser;
		this.target		= target;
		this.idPrefix	= idPrefix;
	}
	
	public static <Args extends IArgs> void check(
		HashMap<Class<?>, TypeParser<?>> typeParser,
		String idPrefix,
		Args target)
	{
		new SanityChecker<>(typeParser, idPrefix, target).check();
	}
	
	private void check()
	{
		checkHasProperties();
		checkHasNecessaryParsers();
		
		for (final var field : target.getClass().getDeclaredFields())
		{
			if (field.isAnnotationPresent(Argument.class))
			{
				final var annotation = field.getAnnotation(Argument.class);
				allArgs.add(annotation);
				if (annotation.isRequired())
				{
					required.add(annotation);
					checkRequiredExcludes(annotation);
				}
				checkBooleanArg(field);
				checkContradictoryRequirement(annotation);
			}
		}
		
		for (final var method : target.getClass().getDeclaredMethods())
		{
			if (method.isAnnotationPresent(Argument.class))
			{
				final var annotation = method.getAnnotation(Argument.class);
				allArgs.add(annotation);
				if (annotation.isRequired())
				{
					required.add(annotation);
					checkRequiredExcludes(annotation);
				}
				
				checkContradictoryRequirement(annotation);
			}
		}
		
		checkDirectContradiction();
		
		/*
		 * TODO analyze if there are contradictory requirements and exclusions.
		 */
		
		if (!issues.isEmpty())
			throw new SanityException(issues);
	}
	
	private void checkRequiredExcludes(Argument annotation)
	{
		if (annotation.excludes().length > 0)
			addIssue(
				"The required argument %s%s excludes the arguments %s and makes them unusable.",
				idPrefix,
				annotation.name(),
				concat(idPrefix, ", ", " & ", annotation.excludes()));
	}
	
	private void checkBooleanArg(Field field)
	{
		try
		{
			field.setAccessible(true);
			
			if (field.getType() != boolean.class)
				return;
			if (!field.getBoolean(target))
				return;
			addIssue(
				"The field %s should not be default initialized with %b, since this makes it impossible for it's value to ever be %b.",
				field.getName(),
				true,
				false);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void checkContradictoryRequirement(Argument annotation)
	{
		for (final var required : annotation.requires())
			for (final var excluded : annotation.excludes())
				if (Objects.equals(required, excluded))
					addIssue(
						"The argument %s%s requires and excludes %s%s!",
						idPrefix,
						annotation.name(),
						idPrefix,
						required);
	}
	
	private void checkDirectContradiction()
	{
		for (final var arg : allArgs)
			for (final var excluded : arg.excludes())
				for (final var req : required)
					if (Objects.equals(req.name(), excluded))
						addIssue(
							"The argument %s%s excludes the required argument %s%s. This makes %s%s unusable.",
							idPrefix,
							arg.name(),
							idPrefix,
							excluded,
							idPrefix,
							arg.name());
					
	}
	
	private void checkHasProperties()
	{
		if (!target.getClass().isAnnotationPresent(ArgsProperties.class))
			System.err.printf(
				"%s should be annotated with @%s!\n\n",
				target.getClass().getName(),
				ArgsProperties.class.getSimpleName());
	}
	
	private void checkHasNecessaryParsers()
	{
		for (final var field : target.getClass().getDeclaredFields())
			if (field.isAnnotationPresent(Argument.class))
				checkHasParser(field.getType());
			
		for (final var method : target.getClass().getDeclaredMethods())
			if (method.isAnnotationPresent(Argument.class))
				for (final var param : method.getParameterTypes())
					checkHasParser(param);
	}
	
	private void checkHasParser(Class<?> type)
	{
		if (!typeParser.containsKey(type))
			throw new MissingParserException(
				"Missing parser for type %s!",
				type.getName());
	}
	
	private static String concat(String idPrefix, String delimiter, String and, String... args)
	{
		if (args.length == 0)
			return "";
		
		if (args.length == 1)
			return idPrefix + args[0];
		
		final var sb = new StringBuilder(idPrefix).append(args[0]);
		for (final var elem : range(1, args.length - 1, args).element())
			sb.append(delimiter).append(idPrefix).append(elem);
		return sb.append(and).append(idPrefix).append(args[args.length - 1]).toString();
	}
	
	private void addIssue(String message, Object... args)
	{
		issues.add(message.formatted(args));
	}
}
