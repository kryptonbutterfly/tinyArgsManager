package test.missing_parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kryptonbutterfly.args.ArgsParser;
import kryptonbutterfly.args.internal.MissingParserException;
import test.misc.Constants;
import test.misc.TerminationException;

public class MissingParser implements Constants
{
	@Test
	public void test()
	{
		final String[] args = { "-o", "TestValue" };
		
		final var parser = new ArgsParser();
		parser.terminateAction	= TerminationException::terminate;
		parser.sanityCheck		= true;
		
		assertThrows(MissingParserException.class, () -> parser.parse(ProgramArgs::new, args));
	}
}
