package test.valid;

import java.util.Objects;

import kryptonbutterfly.args.ArgsProperties;
import kryptonbutterfly.args.Argument;
import kryptonbutterfly.args.IArgs;

@ArgsProperties()
public class ProgramArgs implements IArgs
{
	@Argument(name = "u", info = "The users name.", isRequired = true, requires = "pw")
	public String userName = null;
	
	@Argument(name = "pw", info = "The user password", requires = "u")
	public String password = null;
	
	@Argument(name = "t", info = "The account creation date in unix time.")
	public Long creationTime = null;
	
	@Argument(name = "d", info = "The account creation date", excludes = "t")
	public String creationDate = null;
	
	@Override
	public String programInfo()
	{
		return "This program tests the tiny-args-parser maven package.";
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(creationDate, creationTime, password, userName);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof ProgramArgs))
			return false;
		ProgramArgs other = (ProgramArgs) obj;
		return Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(creationTime, other.creationTime)
				&& Objects.equals(password, other.password)
				&& Objects.equals(userName, other.userName);
	}
	
	@Override
	public String toString()
	{
		return "ProgramArgs [userName="
			+ userName
			+ ", password="
			+ password
			+ ", creationTime="
			+ creationTime
			+ ", creationDate="
			+ creationDate
			+ "]";
	}
}
