package kryptonbutterfly.args;

import static kryptonbutterfly.math.utils.range.Range.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import kryptonbutterfly.monads.opt.Opt;

public final class ArgsParser
{
	private final HashMap<Class<?>, Function<Iterator<String>, ?>>	typeParser	= new HashMap<>();
	private final String											delimiter;
	private final String											idPrefix;
	
	/**
	 * If this is true this parser will sanity check the target before parsing and
	 * throw an exception if a check fails.
	 */
	public boolean sanityCheck = false;
	
	/**
	 * The delimiter is set to {@code ";"}.</br>
	 * The identifier prefix is set to {@code "-"}.
	 * 
	 * @see ArgsParser#ArgsParser(String, String)
	 */
	public ArgsParser()
	{
		this(";", "-");
	}
	
	/**
	 * @param delimiter
	 *            to be used to separate array elements.
	 * @param idPrefix
	 *            the prefix to be expected in the beginning of an argument
	 *            identifier.
	 */
	public ArgsParser(String delimiter, String idPrefix)
	{
		this.delimiter	= delimiter;
		this.idPrefix	= idPrefix;
		addDefaultParser();
	}
	
	/**
	 * @param <T>
	 * @param type
	 *            The type the supplied parser is for.
	 * @param parser
	 */
	public <T> void addParser(Class<T> type, Function<Iterator<String>, T> parser)
	{
		assert !typeParser.containsKey(type) : "A parser for type '%s' has already been added.".formatted(type);
		typeParser.put(type, parser);
	}
	
	/**
	 * @param <Args>
	 * @param constructor
	 *            A function that generates the required object that will be
	 *            populated with data based on the supplied {@code args}.
	 * @param args
	 *            The program arguments.
	 * @return The generated object populated via the supplied {@code args}, or null
	 *         if something went wrong.
	 */
	public <Args extends IArgs> Args parse(Supplier<Args> constructor, String[] args)
	{
		final var target = constructor.get();
		if (sanityCheck)
			sanityCheck(target);
		if (isValid(target, applyArgs(target, args)))
			return target;
		return null;
	}
	
	private final <Args extends IArgs> void sanityCheck(Args target)
	{
		final var	required	= new ArrayList<Argument>();
		final var	allArgs		= new ArrayList<Argument>();
		for (final var field : target.getClass().getDeclaredFields())
		{
			if (field.isAnnotationPresent(Argument.class))
			{
				final var annotation = field.getAnnotation(Argument.class);
				allArgs.add(annotation);
				if (annotation.isRequired())
				{
					required.add(annotation);
					if (annotation.excludes().length > 0)
						throw new SanityException(
							"The required argument %s%s excludes the arguments %s and makes them unusable.",
							idPrefix,
							annotation.name(),
							concat(", ", " & ", annotation.excludes()));
				}
				
				try
				{
					field.setAccessible(true);
					
					if (field.getType() == boolean.class)
					{
						if (field.getBoolean(target))
							throw new SanityException(
								"The field %s should not be default initialized with %b, since this makes it impossible for it's value to ever be %b",
								field.getName(),
								true,
								false);
					}
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		
		for (final var arg : allArgs)
		{
			for (final var excluded : arg.excludes())
			{
				for (final var req : required)
				{
					if (req.name().equals(excluded))
						throw new SanityException(
							"The argument %s%s excludes the required argument %s%s. This makes %s%s unusable.",
							idPrefix,
							arg.name(),
							idPrefix,
							excluded,
							idPrefix,
							arg.name());
				}
			}
		}
		/*
		 * TODO analyze if there are contradictory requirements and exclusions.
		 */
	}
	
	private String concat(String delimiter, String and, String... args)
	{
		if (args.length == 0)
			return "";
		if (args.length == 1)
			return idPrefix + args[0].toString();
		final var sb = new StringBuilder(idPrefix).append(args[0]);
		
		for (final var elem : range(1, args.length - 1, args).element())
			sb.append(delimiter).append(idPrefix).append(elem);
		
		sb.append(and).append(idPrefix).append(args[args.length - 1]);
		return sb.toString();
	}
	
	private final void addDefaultParser()
	{
		addParser(String.class, Iterator::next);
		addParser(long.class, v -> Long.parseLong(v.next()));
		addParser(int.class, v -> Integer.parseInt(v.next()));
		addParser(short.class, v -> Short.parseShort(v.next()));
		addParser(char.class, v -> v.next().charAt(0));
		addParser(byte.class, v -> Byte.parseByte(v.next()));
		addParser(boolean.class, v -> Boolean.parseBoolean(v.next()));
		addParser(double.class, v -> Double.parseDouble(v.next()));
		addParser(float.class, v -> Float.parseFloat(v.next()));
		addParser(String[].class, v -> {
			if (v.hasNext())
				return v.next().split(delimiter);
			return new String[0];
		});
		addParser(long[].class, v -> {
			if (!v.hasNext())
				return new long[0];
			return Stream.of(v.next().split(delimiter))
				.mapToLong(Long::parseLong)
				.toArray();
		});
		addParser(int[].class, v -> {
			if (!v.hasNext())
				return new int[0];
			return Stream.of(v.next().split(delimiter))
				.mapToInt(Integer::parseInt)
				.toArray();
		});
		addParser(short[].class, v -> {
			if (!v.hasNext())
				return new short[0];
			final var	split	= v.next().split(delimiter);
			final var	result	= new short[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Short.parseShort(ie.element());
			return result;
		});
		addParser(byte[].class, v -> {
			if (!v.hasNext())
				return new byte[0];
			final var	split	= v.next().split(delimiter);
			final var	result	= new byte[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Byte.parseByte(ie.element());
			return result;
		});
		addParser(boolean[].class, v -> {
			if (!v.hasNext())
				return new boolean[0];
			final var	split	= v.next().split(delimiter);
			final var	result	= new boolean[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Boolean.parseBoolean(ie.element());
			return result;
		});
		addParser(double[].class, v -> {
			if (!v.hasNext())
				return new double[0];
			return Stream.of(v.next().split(delimiter))
				.mapToDouble(Double::parseDouble)
				.toArray();
		});
		addParser(float[].class, v -> {
			if (!v.hasNext())
				return new float[0];
			final var	split	= v.next().split(delimiter);
			final var	result	= new float[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Float.parseFloat(ie.element());
			return result;
		});
	}
	
	private final <Args extends IArgs> List<String> applyArgs(Args target, String[] args)
	{
		final var	keys		= new ArrayList<String>();
		final var	iterator	= Arrays.asList(args).iterator();
		while (iterator.hasNext())
		{
			var arg = iterator.next();
			if (!this.idPrefix.isBlank())
			{
				if (!arg.startsWith(idPrefix))
				{
					System.out.printf("Unknown argument: %s\n", arg);
					return null;
				}
				arg = arg.substring(idPrefix.length());
			}
			boolean hasArg = injectFields(target, arg, iterator);
			if (!hasArg)
				hasArg = injectMethods(target, arg, iterator);
			
			if (!hasArg)
			{
				System.out.printf("Unknown argument: %s%s\n", idPrefix, arg);
				printInfo(target);
				return null;
			}
			keys.add(arg);
		}
		return keys;
	}
	
	private <Args extends IArgs> boolean injectFields(Args target, String arg, Iterator<String> iterator)
	{
		return Arrays.stream(target.getClass().getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(Argument.class))
			.filter(field -> field.getAnnotation(Argument.class).name().equals(arg))
			.findFirst()
			.filter(field ->
			{
				field.setAccessible(true);
				final var type = field.getType();
				try
				{
					if (type == boolean.class)
						field.set(target, true);
					else
						field.set(
							target,
							Opt.of(typeParser.get(type))
								.map(parser -> parser.apply(iterator))
								.getThrows(missingParser(type)));
					
					return true;
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
				return false;
			})
			.isPresent();
	}
	
	private <Args extends IArgs> boolean injectMethods(Args target, String arg, Iterator<String> iterator)
	{
		return Arrays.stream(target.getClass().getDeclaredMethods())
			.filter(method -> method.isAnnotationPresent(Argument.class))
			.filter(method -> method.getAnnotation(Argument.class).name().equals(arg))
			.findFirst()
			.filter(method ->
			{
				method.setAccessible(true);
				final var paramTypes = method.getParameterTypes();
				final var parameter	= new Object[paramTypes.length];
				for (final var ie : range(paramTypes))
				{
					parameter[ie.index()] = Opt.of(typeParser.get(ie.element()))
						.map(parser -> parser.apply(iterator))
						.getThrows(missingParser(ie.element()));
				}
				try
				{
					method.invoke(target, parameter);
					return true;
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					e.printStackTrace();
				}
				return false;
			})
			.isPresent();
	}
	
	private final <Args extends IArgs> boolean isValid(Args target, List<String> args)
	{
		if (args == null)
			return false;
		
		for (final var arg : args)
		{
			for (final var field : this.getClass().getDeclaredFields())
			{
				final var argument = field.getAnnotation(Argument.class);
				if (argument != null)
					if (!validate(target, arg, args, argument))
						return false;
			}
			for (final var method : this.getClass().getDeclaredMethods())
			{
				final var argument = method.getAnnotation(Argument.class);
				if (argument != null)
					if (!validate(target, arg, args, argument))
						return false;
			}
		}
		
		for (final var field : this.getClass().getDeclaredFields())
		{
			final var argument = field.getAnnotation(Argument.class);
			if (argument != null)
				if (!validate(target, args, argument))
					return false;
		}
		
		for (final var method : this.getClass().getDeclaredMethods())
		{
			final var argument = method.getAnnotation(Argument.class);
			if (argument != null)
				if (!validate(target, args, argument))
					return false;
		}
		return true;
	}
	
	private final <Args extends IArgs> boolean validate(Args target, List<String> args, Argument argument)
	{
		final var message = "The argument \"%s%s\" is a required argument!\n";
		if (argument.isRequired() && !args.contains(argument.name()))
		{
			System.out.printf(message, idPrefix, argument.name());
			printInfo(target);
			return false;
		}
		return true;
	}
	
	private final <Args extends IArgs> boolean validate(Args target, String arg, List<String> args, Argument argument)
	{
		if (argument.name().equals(arg))
		{
			for (final var required : argument.requires())
			{
				if (!args.contains(required))
				{
					final var message = "The argument \"%s%s\" and the argument \"%s%s\" exclude each other!\n";
					System.out.printf(message, idPrefix, arg, idPrefix, required);
					printInfo(target);
					return false;
				}
			}
			for (final var excludes : argument.excludes())
			{
				final var message = "The argument \"%s%s\" and the argument \"%s%s\" exclude each other!\n";
				System.out.printf(message, idPrefix, arg, idPrefix, excludes);
				printInfo(target);
				return false;
			}
		}
		return true;
	}
	
	private final <Args extends IArgs> void printInfo(Args target)
	{
		System.out.printf("%s\n", target.programInfo());
		
		for (final var field : target.getClass().getDeclaredFields())
			Opt.of(field.getAnnotation(Argument.class))
				.if_(argument -> System.out.printf("%s%s\t%s\n", idPrefix, argument.name(), argument.info()));
		
		for (final var method : target.getClass().getDeclaredMethods())
			Opt.of(method.getAnnotation(Argument.class))
				.if_(argument -> System.out.printf("%s%s\t%s\n", idPrefix, argument.name(), argument.info()));
	}
	
	private static Supplier<NoSuchElementException> missingParser(Object target)
	{
		return () -> new NoSuchElementException(
			"Couldn't find a parser for %s! Please register one.".formatted(target));
	}
	
	@SuppressWarnings("serial")
	private static final class SanityException extends RuntimeException
	{
		private SanityException(String message, Object... args)
		{
			super(message.formatted(args));
		}
	}
}
