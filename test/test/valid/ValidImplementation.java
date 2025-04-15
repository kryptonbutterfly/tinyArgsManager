package test.valid;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kryptonbutterfly.args.ArgsParser;
import kryptonbutterfly.args.IArgs;
import test.misc.Constants;
import test.misc.TerminationException;

public class ValidImplementation implements Constants
{
	@Test
	public void test1()
	{
		final var args = new ARGS(null, null, null, null);
		
		final var exception = assertThrows(TerminationException.class, () -> parse(args, ProgramArgs::new));
		assertEquals(0, exception.status, EXIT_CODE);
	}
	
	@Test
	public void test2()
	{
		final var args = new ARGS("kryptonbutterfly", null, null, null);
		
		final var exception = assertThrows(TerminationException.class, () -> parse(args, ProgramArgs::new));
		assertEquals(0, exception.status, EXIT_CODE);
	}
	
	@Test
	public void test3()
	{
		final var args = new ARGS("kryptonbutterfly", "pw-1234", null, null);
		
		final var arguments = assertDoesNotThrow(() -> parse(args, ProgramArgs::new));
		assertEquals(args.expected, arguments, "Mismatch");
	}
	
	@Test
	public void test4()
	{
		final var args = new ARGS("kryptonbutterfly", "pw-1234", 123456789L, null);
		
		final var arguments = assertDoesNotThrow(() -> parse(args, ProgramArgs::new));
		assertEquals(args.expected, arguments, "Mismatch");
	}
	
	@Test
	public void test5()
	{
		final var args = new ARGS("kryptonbutterfly", "pw-1234", null, "March 13 2054");
		
		final var arguments = assertDoesNotThrow(() -> parse(args, ProgramArgs::new));
		assertEquals(args.expected, arguments, "Mismatch");
	}
	
	@Test
	public void test6()
	{
		final var args = new ARGS("kryptonbutterfly", "pw-1234", 123456789L, "March 14 2054");
		
		final var exception = assertThrows(TerminationException.class, () -> parse(args, ProgramArgs::new));
		assertEquals(0, exception.status, EXIT_CODE);
	}
	
	@Test
	public void test7()
	{
		final String[] args = { "-i", "invalid parameter!" };
		
		final var exception = assertThrows(TerminationException.class, () -> parse(args, ProgramArgs::new));
		assertEquals(0, exception.status, EXIT_CODE);
	}
	
	@Test
	public void testHelp()
	{
		final String[] args = { "-h" };
		
		final var exception = assertThrows(TerminationException.class, () -> parse(args, ProgramArgs::new));
		assertEquals(0, exception.status, EXIT_CODE);
	}
	
	private final <T extends IArgs> T parse(String[] args, Supplier<T> constructor)
	{
		final var parser = new ArgsParser();
		parser.terminateAction	= TerminationException::terminate;
		parser.sanityCheck		= true;
		return parser.parse(constructor, args);
		
	}
	
	private final <T extends IArgs> T parse(ARGS args, Supplier<T> constructor)
	{
		final var parser = new ArgsParser();
		parser.terminateAction	= TerminationException::terminate;
		parser.sanityCheck		= true;
		return parser.parse(constructor, args.toArgs());
	}
	
	private static final class ARGS
	{
		public final ProgramArgs expected;
		
		ARGS(String userName, String password, Long creationTime, String creationDate)
		{
			this.expected				= new ProgramArgs();
			this.expected.userName		= userName;
			this.expected.password		= password;
			this.expected.creationTime	= creationTime;
			this.expected.creationDate	= creationDate;
		}
		
		public String[] toArgs()
		{
			final var list = new ArrayList<Object>();
			if (expected.userName != null)
			{
				list.add("-u");
				list.add(expected.userName);
			}
			if (expected.password != null)
			{
				list.add("-pw");
				list.add(expected.password);
			}
			if (expected.creationTime != null)
			{
				list.add("-t");
				list.add(expected.creationTime);
			}
			if (expected.creationDate != null)
			{
				list.add("-d");
				list.add(expected.creationDate);
			}
			
			return list.stream().map(Object::toString).toArray(String[]::new);
		}
	}
}
