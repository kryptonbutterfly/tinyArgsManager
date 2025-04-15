package kryptonbutterfly.args;

import static kryptonbutterfly.math.utils.range.Range.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import kryptonbutterfly.args.internal.SanityChecker;
import kryptonbutterfly.monads.opt.Opt;

public final class ArgsParser
{
	private final HashMap<Class<?>, TypeParser<?>> typeParser = new HashMap<>();
	
	/**
	 * If this is true this parser will sanity check the target before parsing and
	 * throw an exception if a check fails.
	 */
	public boolean sanityCheck = false;
	
	private final String arrayDelimiter;
	
	public IntConsumer terminateAction = (int status) -> System.exit(status);
	
	/**
	 * initializes {@code arrayDelimiter} with {@code ;}
	 * 
	 * @see ArgsParser#ArgsParser(String)
	 */
	public ArgsParser()
	{
		this(";");
	}
	
	/**
	 * @param arrayDelimiter
	 *            The delimiter to be used to separate array elements.
	 */
	public ArgsParser(String arrayDelimiter)
	{
		this.arrayDelimiter = arrayDelimiter;
		addDefaultParser();
	}
	
	/**
	 * @param <T>
	 * @param type
	 *            The type the supplied parser is for.
	 * @param parser
	 */
	public <T> void addParser(Class<T> type, TypeParser<T> parser)
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
		return new ParseRun<Args>(target, args).parse();
	}
	
	private final class ParseRun<Args extends IArgs>
	{
		private final Args				target;
		private final String[]			args;
		private final ArgsProperties	props;
		
		ParseRun(Args target, String[] args)
		{
			this.target	= target;
			this.args	= args;
			this.props	= getProperties(target);
		}
		
		private final ArgsProperties getProperties(Args target)
		{
			return Opt.of(target.getClass().getAnnotation(ArgsProperties.class))
				.get(() -> IArgs.class.getAnnotation(ArgsProperties.class));
		}
		
		private final Args parse()
		{
			if (sanityCheck)
				SanityChecker.check(ArgsParser.this.typeParser, props.idPrefix(), target);
			
			final var argsResult = applyArgs();
			if (argsResult == null)
				return null;
			
			final var prop = getProperties(target);
			
			if (prop.terminateAfterHelp())
			{
				if (argsResult.printHelp())
					target.printHelp(ArgsParser.this);
				return isValid(argsResult.args()) ? target : null;
			}
			else
			{
				if (!isValid(argsResult.args()))
					terminateAction.accept(-1);
				
				if (argsResult.printHelp())
					target.printHelp(ArgsParser.this);
				return target;
			}
		}
		
		private final ArgsResult applyArgs()
		{
			final var	keys		= new ArrayList<String>();
			final var	iterator	= Arrays.asList(args).iterator();
			boolean		printHelp	= false;
			while (iterator.hasNext())
			{
				var arg = iterator.next();
				if (!props.idPrefix().isBlank())
				{
					if (!arg.startsWith(props.idPrefix()))
					{
						System.out.printf("Unknown argument: %s\n\n", arg);
						target.printHelp(ArgsParser.this);
						return null;
					}
					arg = arg.substring(props.idPrefix().length());
				}
				
				boolean isHelpArg = false;
				for (final var id : props.helpIDs())
					if (id.equals(arg))
						isHelpArg = true;
					
				if (isHelpArg)
					printHelp = true;
				else
				{
					boolean hasArg = injectFields(arg, iterator);
					if (!hasArg)
						hasArg = injectMethods(arg, iterator);
					
					if (!hasArg)
					{
						System.out.printf("Unknown argument: %s%s\n\n", props.idPrefix(), arg);
						target.printHelp(ArgsParser.this);
						return null;
					}
					keys.add(arg);
				}
			}
			return new ArgsResult(keys, printHelp);
		}
		
		private final boolean isValid(List<String> args)
		{
			if (args == null)
				return false;
			
			for (final var arg : args)
			{
				for (final var field : target.getClass().getDeclaredFields())
				{
					final var argument = field.getAnnotation(Argument.class);
					if (argument != null)
						if (!validate(arg, args, argument))
							return false;
				}
				for (final var method : target.getClass().getDeclaredMethods())
				{
					final var argument = method.getAnnotation(Argument.class);
					if (argument != null)
						if (!validate(arg, args, argument))
							return false;
				}
			}
			
			for (final var field : target.getClass().getDeclaredFields())
			{
				final var argument = field.getAnnotation(Argument.class);
				if (argument != null)
					if (!validate(args, argument))
						return false;
			}
			
			for (final var method : target.getClass().getDeclaredMethods())
			{
				final var argument = method.getAnnotation(Argument.class);
				if (argument != null)
					if (!validate(args, argument))
						return false;
			}
			return true;
		}
		
		private final boolean validate(List<String> args, Argument argument)
		{
			final var message = "The argument \"%s%s\" is a required argument!\n\n";
			if (argument.isRequired() && !args.contains(argument.name()))
			{
				System.out.printf(message, props.idPrefix(), argument.name());
				target.printHelp(ArgsParser.this);
				return false;
			}
			return true;
		}
		
		private final boolean validate(String arg, List<String> args, Argument argument)
		{
			if (argument.name().equals(arg))
			{
				for (final var required : argument.requires())
					if (!args.contains(required))
					{
						final var message = "The argument \"%s%s\" requires the argument \"%s%s\"!\n\n";
						System.out.printf(message, props.idPrefix(), arg, props.idPrefix(), required);
						target.printHelp(ArgsParser.this);
						return false;
					}
				
				for (final var excludes : argument.excludes())
					if (args.contains(excludes))
					{
						final var message = "The argument \"%s%s\" and \"%s%s\" exclude each other!\n\n";
						System.out.printf(message, props.idPrefix(), arg, props.idPrefix(), excludes);
						target.printHelp(ArgsParser.this);
						return false;
					}
			}
			return true;
		}
		
		private boolean injectFields(String arg, Iterator<String> iterator)
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
									.map(parser -> parser.parse(arrayDelimiter, iterator))
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
		
		private boolean injectMethods(String arg, Iterator<String> iterator)
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
						parameter[ie.index()] = Opt.of(typeParser.get(ie.element()))
							.map(parser -> parser.parse(arrayDelimiter, iterator))
							.getThrows(missingParser(ie.element()));
					
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
	}
	
	private final void addDefaultParser()
	{
		addParser(String.class, (d, v) -> v.next());
		addParser(long.class, (d, v) -> Long.parseLong(v.next()));
		addParser(Long.class, (d, v) -> Long.valueOf(v.next()));
		addParser(int.class, (d, v) -> Integer.parseInt(v.next()));
		addParser(Integer.class, (d, v) -> Integer.valueOf(v.next()));
		addParser(short.class, (d, v) -> Short.parseShort(v.next()));
		addParser(Short.class, (d, v) -> Short.valueOf(v.next()));
		addParser(char.class, (d, v) -> v.next().charAt(0));
		addParser(Character.class, (d, v) -> Character.valueOf(v.next().charAt(0)));
		addParser(byte.class, (d, v) -> Byte.parseByte(v.next()));
		addParser(Byte.class, (d, v) -> Byte.valueOf(Byte.parseByte(v.next())));
		addParser(boolean.class, (d, v) -> Boolean.parseBoolean(v.next()));
		addParser(Boolean.class, (d, v) -> Boolean.valueOf(v.next()));
		addParser(double.class, (d, v) -> Double.parseDouble(v.next()));
		addParser(Double.class, (d, v) -> Double.valueOf(v.next()));
		addParser(float.class, (d, v) -> Float.parseFloat(v.next()));
		addParser(Float.class, (d, v) -> Float.valueOf(v.next()));
		addParser(String[].class, (d, v) -> v.hasNext() ? v.next().split(d) : new String[0]);
		addParser(long[].class, (d, v) -> {
			if (!v.hasNext())
				return new long[0];
			return Stream.of(v.next().split(d))
				.mapToLong(Long::parseLong)
				.toArray();
		});
		addParser(int[].class, (d, v) -> {
			if (!v.hasNext())
				return new int[0];
			return Stream.of(v.next().split(d))
				.mapToInt(Integer::parseInt)
				.toArray();
		});
		addParser(short[].class, (d, v) -> {
			if (!v.hasNext())
				return new short[0];
			final var	split	= v.next().split(d);
			final var	result	= new short[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Short.parseShort(ie.element());
			return result;
		});
		addParser(byte[].class, (d, v) -> {
			if (!v.hasNext())
				return new byte[0];
			final var	split	= v.next().split(d);
			final var	result	= new byte[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Byte.parseByte(ie.element());
			return result;
		});
		addParser(boolean[].class, (d, v) -> {
			if (!v.hasNext())
				return new boolean[0];
			final var	split	= v.next().split(d);
			final var	result	= new boolean[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Boolean.parseBoolean(ie.element());
			return result;
		});
		addParser(double[].class, (d, v) -> {
			if (!v.hasNext())
				return new double[0];
			return Stream.of(v.next().split(d))
				.mapToDouble(Double::parseDouble)
				.toArray();
		});
		addParser(float[].class, (d, v) -> {
			if (!v.hasNext())
				return new float[0];
			final var	split	= v.next().split(d);
			final var	result	= new float[split.length];
			for (final var ie : range(split))
				result[ie.index()] = Float.parseFloat(ie.element());
			return result;
		});
	}
	
	private static Supplier<NoSuchElementException> missingParser(Object target)
	{
		return () -> new NoSuchElementException(
			"Couldn't find a parser for %s! Please register one.".formatted(target));
	}
	
	private static final record ArgsResult(List<String> args, boolean printHelp)
	{}
}
