
package bridge.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
	APITest.class,
	BeanResourceTest.class,
	HelloTest.class,
	MethodsTest.class,
	ValidatedRestTest.class,
	ValidatedTest.class,
} )
public abstract class AllTests
{
}
