package test.missing_parser;

import kryptonbutterfly.args.ArgsProperties;
import kryptonbutterfly.args.Argument;
import kryptonbutterfly.args.IArgs;

@ArgsProperties()
public class ProgramArgs implements IArgs
{
	@Argument(name = "o", info = "object")
	public Object object = null;
	
	@Override
	public String toString()
	{
		return "ProgramArgs_MissingParser [object=" + object + "]";
	}
	
	@Override
	public String programInfo()
	{
		return "This program test the tiny-args-parser maven package.";
	}
}
