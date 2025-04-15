package test.contradictory;

import kryptonbutterfly.args.ArgsProperties;
import kryptonbutterfly.args.Argument;
import kryptonbutterfly.args.IArgs;

@ArgsProperties
public class IsRequired_Unusable implements IArgs
{
	@Argument(name = "1", info = "1. argument", excludes = "2", isRequired = true)
	public String arg1 = null;
	
	@Argument(name = "2", info = "2. argument")
	public String arg2 = null;
	
	@Override
	public String programInfo()
	{
		return "IsRequired makes argument unusable.";
	}
}
