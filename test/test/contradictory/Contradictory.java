package test.contradictory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kryptonbutterfly.args.ArgsParser;
import kryptonbutterfly.args.internal.SanityException;
import test.misc.Constants;
import test.misc.TerminationException;

public class Contradictory implements Constants
{
	@Test
	public void isRequiredUnusable()
	{
		final var parser = new ArgsParser();
		parser.terminateAction	= TerminationException::terminate;
		parser.sanityCheck		= true;
		
		final String[] args = {};
		
		assertThrows(SanityException.class, () -> parser.parse(IsRequired_Unusable::new, args));
	}
	
	@Test
	public void contradiction()
	{
		final var parser = new ArgsParser();
		parser.terminateAction	= TerminationException::terminate;
		parser.sanityCheck		= true;
		
		final String[] args = { "-2", "value for arg2" };
		
		final var exception = assertThrows(TerminationException.class, () -> parser.parse(Contradiction::new, args));
		assertEquals(0, exception.status, EXIT_CODE);
	}
}
