package test.missing_properties;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kryptonbutterfly.args.ArgsParser;
import test.misc.Constants;
import test.misc.TerminationException;

public class MissingProperties implements Constants
{
	@Test
	public void test()
	{
		final var parser = new ArgsParser();
		parser.terminateAction	= TerminationException::terminate;
		parser.sanityCheck		= true;
		
		final var exception = assertThrows(
			TerminationException.class,
			() -> parser.parse(ProgramArgs::new, new String[0]));
		assertEquals(0, exception.status, EXIT_CODE);
	}
}