package kryptonbutterfly.args;

import static kryptonbutterfly.math.utils.range.Range.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import kryptonbutterfly.monads.opt.Opt;

public abstract class AProgramArguments
{
	private final HashMap<Class<?>, Function<Iterator<String>, ?>>	allParser	= new HashMap<>();
	private String													programInfo	= "";
	private final String											delimiter;
	
	protected AProgramArguments(String[] args, String programInfo)
	{
		this(args, programInfo, ";");
	}
	
	protected AProgramArguments(String[] args, String programInfo, String delimiter)
	{
		this.delimiter		= delimiter;
		this.programInfo	= programInfo;
		// Logger.info("ArgsManager: adding default parser.");
		this.addStandardParser();
		// Logger.info("ArgsManager: adding custom parser.");
		this.addAllParser();
		this.setDefaults();
		try
		{
			this.validate(this.inject(args));
		}
		catch (IllegalArgumentException e)
		{
			// Logger.error(e);
			System.out.println(e.getMessage());
			System.out.println();
			this.printInfo();
			// Logger.trace("ArgsManager: Terminating application! - Not existing argument
			// supplied!");
			e.printStackTrace();
			System.exit(-1);
		}
		catch (NoSuchElementException e)
		{
			// Logger.error(e);
			this.printInfo();
			// Logger.trace("ArgsManager: Terminating application!");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void addStandardParser()
	{
		addParser(String.class, value -> value.next());
		addParser(long.class, value -> Long.parseLong(value.next()));
		addParser(int.class, value -> Integer.parseInt(value.next()));
		addParser(short.class, value -> Short.parseShort(value.next()));
		addParser(char.class, value -> value.next().charAt(0));
		addParser(byte.class, value -> Byte.parseByte(value.next()));
		addParser(boolean.class, value -> Boolean.parseBoolean(value.next()));
		addParser(double.class, value -> Double.parseDouble(value.next()));
		addParser(float.class, value -> Float.parseFloat(value.next()));
		addParser(String[].class, value -> {
			if (value.hasNext())
			{
				return value.next().split(delimiter);
			}
			return new String[0];
		});
		addParser(long[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new long[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Long.parseLong(split[i]);
				}
				return result;
			}
			return new long[0];
		});
		addParser(int[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new int[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Integer.parseInt(split[i]);
				}
				return result;
			}
			return new int[0];
		});
		addParser(short[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new short[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Short.parseShort(split[i]);
				}
				return result;
			}
			return new short[0];
		});
		addParser(char[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new char[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = split[i].charAt(0);
				}
				return result;
			}
			return new char[0];
		});
		addParser(byte[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new byte[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Byte.parseByte(split[i]);
				}
				return result;
			}
			return new byte[0];
		});
		addParser(boolean[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new boolean[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Boolean.parseBoolean(split[i]);
				}
				return result;
			}
			return new boolean[0];
		});
		addParser(double[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new double[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Double.parseDouble(split[i]);
				}
				return result;
			}
			return new double[0];
		});
		addParser(float[].class, value -> {
			if (value.hasNext())
			{
				final var	split	= value.next().split(delimiter);
				final var	result	= new float[split.length];
				for (int i = 0; i < split.length; i++)
				{
					result[i] = Float.parseFloat(split[i]);
				}
				return result;
			}
			return new float[0];
		});
	}
	
	/**
	 * Implement this method to supply custom parser. This method will be called on
	 * creation of this object.
	 */
	protected abstract void addAllParser();
	
	/**
	 * Override this method to set default values for your arguments.
	 */
	protected void setDefaults()
	{};
	
	private final void validate(List<String> args)
	{
		// Logger.info("ArgsManager: Validating parsed program-arguments.");
		for (final var arg : args)
		{
			for (final var field : this.getClass().getDeclaredFields())
			{
				Opt.of(field.getAnnotation(Argument.class))
					.if_(argument -> this.validate(arg, args, argument));
			}
			for (final var method : this.getClass().getDeclaredMethods())
			{
				Opt.of(method.getAnnotation(Argument.class))
					.if_(argument -> this.validate(arg, args, argument));
			}
		}
		
		for (final var field : this.getClass().getDeclaredFields())
		{
			Opt.of(field.getAnnotation(Argument.class)).if_(argument -> validate(args, argument));
		}
		
		for (final var method : this.getClass().getDeclaredMethods())
		{
			Opt.of(method.getAnnotation(Argument.class)).if_(argument -> validate(args, argument));
		}
	}
	
	private final void validate(List<String> args, Argument argument)
	{
		final var	s		= Argument.requiresMinusAtIdentifier ? "-" : "";
		final var	message	= "The argument \"%s%s\" is a required argument!\n";
		if (argument.isRequired() && !args.contains(argument.name()))
		{
			System.out.printf(message, s, argument.name());
			this.printInfo();
			System.exit(-1);
		}
	}
	
	private final void validate(String arg, List<String> args, Argument argument)
	{
		final var s = Argument.requiresMinusAtIdentifier ? "-" : "";
		if (argument.name().equals(arg))
		{
			for (final var required : argument.requires())
			{
				if (!args.contains(required))
				{
					final var message = "The argument \"%s%s\" requires the argument \"%s%s\" to be supplied!\n";
					System.out.printf(message, s, arg, s, required);
					this.printInfo();
					// Logger.trace("ArgsManager: Terminating application! - required Argument not
					// supplied!");
					System.exit(-1);
				}
			}
			for (final var excluded : argument.excludes())
			{
				if (args.contains(excluded))
				{
					final var message = "The argument \"%s%s\" and the argument \"%s%s\" exclude each other!\n";
					System.out.printf(message, s, arg, s, excluded);
					this.printInfo();
					// Logger.trace("ArgsManager: Terminating application! - excluded Argument
					// supplied!");
					System.exit(-1);
				}
			}
		}
	}
	
	public final void printInfo()
	{
		// Logger.info("ArgsManager: Printing program-argument info to console.");
		System.out.println(this.programInfo + "\n");
		final var s = Argument.requiresMinusAtIdentifier ? "-" : "";
		for (final var field : this.getClass().getDeclaredFields())
		{
			Opt.of(field.getAnnotation(Argument.class))
				.if_(argument -> System.out.println(s + argument.name() + "\t" + argument.info()));
		}
		for (final var method : this.getClass().getDeclaredMethods())
		{
			Opt.of(method.getAnnotation(Argument.class))
				.if_(argument -> System.out.println(s + argument.name() + "\t" + argument.info()));
		}
	}
	
	/**
	 * Call this method only in {@link #addAllParser()}!
	 * 
	 * @param name
	 *            The type the parser returns
	 * @param parser
	 */
	protected final <T> void addParser(Class<T> name, Function<Iterator<String>, T> parser)
	{
		allParser.put(name, parser);
		// Logger.info("ArgsManager: added parser for " + name);
	}
	
	private List<String> inject(String[] args)
	{
		// Logger.info("ArgsManager: Parsing arguments and injecting into fields.");
		final List<String>	keys		= new LinkedList<>();
		final var			arguments	= Arrays.asList(args);
		for (final var iterator = arguments.iterator(); iterator.hasNext();)
		{
			var arg = iterator.next();
			if (Argument.requiresMinusAtIdentifier)
			{
				if (arg.isEmpty() || arg.charAt(0) != '-')
				{
					throw new IllegalArgumentException("Unknown Argument: " + arg);
				}
				arg = arg.substring(1);
			}
			boolean hasArg = this.injectFields(arg, iterator);
			if (!hasArg)
			{
				hasArg = this.injectMethods(arg, iterator);
			}
			if (!hasArg)
			{
				throw new IllegalArgumentException(
					"Unknown Argument: " + (Argument.requiresMinusAtIdentifier ? "-" : "") + arg);
			}
			keys.add(arg);
		}
		return keys;
	}
	
	private boolean injectFields(String arg, Iterator<String> iterator)
	{
		return Arrays.stream(this.getClass().getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(Argument.class))
			.filter(field -> field.getAnnotation(Argument.class).name().equals(arg))
			.findAny()
			.filter(field ->
			{
				field.setAccessible(true);
				final var type = field.getType();
				try
				{
					if (type != boolean.class)
					{
						field.set(
							this,
							Opt.of(allParser.get(type))
								.map(parser -> parser.apply(iterator))
								.getThrows(missingParser(type)));
					}
					else
					{
						field.set(this, true);
					}
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
		return Arrays.stream(this.getClass().getDeclaredMethods())
			.filter(method -> method.isAnnotationPresent(Argument.class))
			.filter(method -> method.getAnnotation(Argument.class).name().equals(arg))
			.findAny()
			.filter(method ->
			{
				method.setAccessible(true);
				final var paramTypes = method.getParameterTypes();
				final var parameter	= new Object[paramTypes.length];
				for (final var ie : range(paramTypes))
				{
					parameter[ie.index()] = Opt.of(allParser.get(ie.element()))
						.map(parser -> parser.apply(iterator))
						.getThrows(missingParser(ie.element()));
				}
				try
				{
					method.invoke(this, parameter);
					return true;
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
				return false;
			})
			.isPresent();
	}
	
	private static Supplier<NoSuchElementException> missingParser(Object target)
	{
		return () -> new NoSuchElementException(
			"Couldn't find a parser for %s! Please register one.".formatted(target));
	}
}