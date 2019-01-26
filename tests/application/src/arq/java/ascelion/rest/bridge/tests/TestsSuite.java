
package ascelion.rest.bridge.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
	AbortAPITest.class,
	AbortAsyncAPITest.class,
	APITest.class,
	AsyncTest.class,
	BeanResourceTest.class,
	ClientHeadersTest.class,
	ConvertTest.class,
	HelloTest.class,
	MethodsTest.class,
	TimeoutTest.class,
	UsersTest.class,
	ValidatedRestTest.class,
	ValidatedTest.class,
} )
public class TestsSuite
{
}
