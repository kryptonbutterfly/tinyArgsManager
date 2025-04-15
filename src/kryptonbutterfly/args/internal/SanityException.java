package kryptonbutterfly.args.internal;

import java.util.ArrayList;

@SuppressWarnings("serial")
public final class SanityException extends RuntimeException
{
	SanityException(ArrayList<String> issues)
	{
		super(concat(issues));
	}
	
	private static String concat(ArrayList<String> issues)
	{
		final var sb = new StringBuilder();
		for (final var issue : issues)
			sb.append(issue).append("\n\n");
		
		return sb.toString();
	}
}
