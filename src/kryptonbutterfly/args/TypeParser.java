package kryptonbutterfly.args;

import java.util.Iterator;

@FunctionalInterface
public interface TypeParser<T>
{
	public T parse(String arrayDelimiter, Iterator<String> iterator);
}
