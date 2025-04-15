package test.misc;

@SuppressWarnings("serial")
public class TerminationException extends RuntimeException
{
	public final int status;
	
	public TerminationException(int status)
	{
		super("This exception should be caught in a test and the status validated!");
		this.status = status;
	}
	
	public static void terminate(int status)
	{
		throw new TerminationException(status);
	}
}
