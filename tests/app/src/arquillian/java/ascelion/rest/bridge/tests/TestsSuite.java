
package ascelion.rest.bridge.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
	APITest.class,
	BeanResourceTest.class,
	HelloTest.class,
	MethodsTest.class,
	TimeoutTest.class,
	UsersTest.class,
	ValidatedRestTest.class,
	ValidatedTest.class,
} )
public abstract class TestsSuite
{
}
