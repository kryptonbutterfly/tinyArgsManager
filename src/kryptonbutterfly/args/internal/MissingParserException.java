package kryptonbutterfly.args.internal;

@SuppressWarnings("serial")
public class MissingParserException extends RuntimeException
{
	public MissingParserException(String msg, Object... args)
	{
		super(msg.formatted(args));
	}
}
