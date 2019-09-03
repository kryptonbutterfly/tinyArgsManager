package de.tinycodecrank.args;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import de.tinycodecrank.utils.nullable.Optional;

//import de.dummy.logger.Logger;

public abstract class AProgramArguments
{
	private static final HashMap<Class<?>, Function<Iterator<String>, ?>> allParser = new HashMap<>();
	private String programInfo = "";
	private static String delimiter = "\\|";

	protected AProgramArguments(String [] args, String programInfo)
	{
		this.programInfo = programInfo;
//		Logger.info("ArgsManager: adding default parser.");
		this.addStandardParser();
//		Logger.info("ArgsManager: adding custom parser.");
		this.addAllParser();
		try
		{
			this.validate(this.inject(args));
		}
		catch (IllegalArgumentException e)
		{
//			Logger.error(e);
			System.out.println(e.getMessage());
			System.out.println();
			this.printInfo();
//			Logger.trace("ArgsManager: Terminating application! - Not existing argument supplied!");
			e.printStackTrace();
			System.exit(-1);
		}
		catch (NoSuchElementException e)
		{
//			Logger.error(e);
			this.printInfo();
//			Logger.trace("ArgsManager: Terminating application!");
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
		addParser(String [].class, value -> value.next().split(delimiter));
		addParser(long [].class, value -> {
			String [] split = value.next().split(delimiter);
			long [] result = new long [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Long.parseLong(split[i]);
			}
			return result;
		});
		addParser(int [].class, value -> {
			String [] split = value.next().split(delimiter);
			int [] result = new int [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Integer.parseInt(split[i]);
			}
			return result;
		});
		addParser(short [].class, value -> {
			String [] split = value.next().split(delimiter);
			short [] result = new short [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Short.parseShort(split[i]);
			}
			return result;
		});
		addParser(char [].class, value -> {
			String [] split = value.next().split(delimiter);
			char [] result = new char [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = split[i].charAt(0);
			}
			return result;
		});
		addParser(byte [].class, value -> {
			String [] split = value.next().split(delimiter);
			byte [] result = new byte [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Byte.parseByte(split[i]);
			}
			return result;
		});
		addParser(boolean [].class, value -> {
			String [] split = value.next().split(delimiter);
			boolean [] result = new boolean [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Boolean.parseBoolean(split[i]);
			}
			return result;
		});
		addParser(double [].class, value -> {
			String [] split = value.next().split(delimiter);
			double [] result = new double [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Double.parseDouble(split[i]);
			}
			return result;
		});
		addParser(float [].class, value -> {
			String [] split = value.next().split(delimiter);
			float [] result = new float [split.length];
			for (int i = 0; i < split.length; i++)
			{
				result[i] = Float.parseFloat(split[i]);
			}
			return result;
		});
	}

	/**
	 * Implement this method to supply custom parser.
	 * This method will be called on creation of this object.
	 */
	protected abstract void addAllParser();

	private final void validate(List<String> args)
	{
//		Logger.info("ArgsManager: Validating parsed program-arguments.");
		for (String arg : args)
		{
			for (Field field : this.getClass().getDeclaredFields())
			{
				Optional.of(field.getAnnotation(Argument.class))
					.peek(argument -> this.validate(arg, args, argument));
			}
			for (Method method : this.getClass().getDeclaredMethods())
			{
				Optional.of(method.getAnnotation(Argument.class))
					.peek(argument -> this.validate(arg, args, argument));
			}
		}
	}

	private final void validate(String arg, List<String> args, Argument argument)
	{
		String s = Argument.requiresMinusAtIdentifier ? "-" : "";
		if (argument.name().equals(arg))
		{
			for (String required : argument.requires())
			{
				if (!args.contains(required))
				{
					System.out.println("The argument \"" + s + arg + "\" requires the Argument \"" + s + required + "\" to be supplied!");
					this.printInfo();
//					Logger.trace("ArgsManager: Terminating application! - required Argument not supplied!");
					System.exit(-1);
				}
			}
			for (String excluded : argument.excludes())
			{
				if (args.contains(excluded))
				{
					System.out.println("The argument \"" + s + arg + "\" and the Argument \"" + s + excluded + "\" exclude each other!");
					this.printInfo();
//					Logger.trace("ArgsManager: Terminating application! - excluded Argument supplied!");
					System.exit(-1);
				}
			}
		}
	}

	public final void printInfo()
	{
//		Logger.info("ArgsManager: Printing program-argument info to console.");
		System.out.println(this.programInfo + "\n");
		String s = Argument.requiresMinusAtIdentifier ? "-" : "";
		for (Field field : this.getClass().getDeclaredFields())
		{
			Optional.of(field.getAnnotation(Argument.class))
				.peek(argument ->
					System.out.println(s + argument.name() + "\t" + argument.info()));
		}
		for (Method method : this.getClass().getDeclaredMethods())
		{
			Optional.of(method.getAnnotation(Argument.class))
				.peek(argument ->
					System.out.println(s + argument.name() + "\t" + argument.info()));
		}
	}

	/**
	 * Call this method only in {@link #addAllParser()}!
	 * 
	 * @param name
	 *            The type the parser returns
	 * @param parser
	 */
	protected static final <T> void addParser(Class<T> name, Function<Iterator<String>, T> parser)
	{
		allParser.put(name, parser);
//		Logger.info("ArgsManager: added parser for " + name);
	}

	private List<String> inject(String [] args)
	{
//		Logger.info("ArgsManager: Parsing arguments and injecting into fields.");
		List<String> keys = new LinkedList<>();
		List<String> arguments = Arrays.asList(args);
		for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext();)
		{
			String arg = iterator.next();
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
				throw new IllegalArgumentException("Unknown Argument: " + (Argument.requiresMinusAtIdentifier ? "-" : "") + arg);
			}
			keys.add(arg);
		}
		return keys;
	}

	private boolean injectFields(String arg, Iterator<String> iterator)
	{
		Field [] fields = this.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			if (field.isAnnotationPresent(Argument.class))
			{
				Argument argument = field.getAnnotation(Argument.class);
				if (argument.name().equals(arg))
				{
					field.setAccessible(true);
					try
					{
						if (field.getType() != boolean.class)
						{
							Optional.of(allParser.get(field.getType()))
								.ifOrElse(parser -> parser.apply(iterator),
									() -> {
										throw new NullPointerException("Couldn't find a parser for " + field.getType() + "! Please register one.");
									});
						}
						else
						{
							field.set(this, true);
						}
						return true;
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
//						Logger.error(e);
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	private boolean injectMethods(String arg, Iterator<String> iterator)
	{
		Method [] methods = this.getClass().getDeclaredMethods();
		for (Method method : methods)
		{
			if (method.isAnnotationPresent(Argument.class))
			{
				Argument argument = method.getAnnotation(Argument.class);
				if (argument.name().equals(arg))
				{
					method.setAccessible(true);
					try
					{
						Class<?> [] paramTypes = method.getParameterTypes();
						Object [] parameter = new Object [paramTypes.length];
						for (int i = 0; i < paramTypes.length; i++)
						{
							final int index = i;
							Optional.of(allParser.get(paramTypes[index]))
								.ifOrElse(parser -> parameter[index] = parser.apply(iterator),
									() -> {
										throw new NullPointerException("Couldn't find a parser for " + paramTypes[index] + "! Please register one.");
									});
						}
						method.invoke(this, parameter);
						return true;
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
//						Logger.error(e);
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}
		return false;
	}

	public static final void setCollectionDelimiter(String delimiter)
	{
		AProgramArguments.delimiter = delimiter;
	}
}