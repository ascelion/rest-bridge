
package bridge.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
	BeanResourceSuite.class,
	HelloSuite.class,
	MethodsSuite.class,
	ValidatedSuite.class,
} )
public class WholeSuite
{
}
