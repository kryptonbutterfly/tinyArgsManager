package test.contradictory;

import kryptonbutterfly.args.ArgsProperties;
import kryptonbutterfly.args.Argument;
import kryptonbutterfly.args.IArgs;

@ArgsProperties
public class Contradiction implements IArgs
{
	@Argument(name = "1", info = "1. argument", excludes = "2")
	public String arg1 = null;
	
	@Argument(name = "2", info = "2. argument", requires = "1")
	public String arg2 = null;
	
	@Override
	public String programInfo()
	{
		return "Contradictory requirement.";
	}
}
