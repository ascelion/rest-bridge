
package bridge.tests.payara;

import java.util.logging.Level;
import java.util.logging.Logger;

import bridge.tests.AllProvidersTests;

public class PayaraSuite
extends AllProvidersTests
{

	static {
		Logger.getLogger( "ascelion" ).setLevel( Level.ALL );
	}
}
