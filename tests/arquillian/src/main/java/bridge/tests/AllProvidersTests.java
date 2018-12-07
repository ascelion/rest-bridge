
package bridge.tests;

import bridge.tests.providers.JerseyBridgeProvider;
import bridge.tests.providers.JerseyProxyProvider;
import bridge.tests.providers.ResteasyBridgeProvider;
import bridge.tests.providers.ResteasyProxyProvider;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
//	AllProvidersTests.JerseyBridge.class,
//	AllProvidersTests.JerseyProxy.class,
//	AllProvidersTests.ResteasyBridge.class,
	AllProvidersTests.ResteasyProxy.class,
} )
public class AllProvidersTests
{

	@RunWith( Suite.class )
	@SuiteClasses( AllTests.class )
	static public class JerseyBridge
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new JerseyBridgeProvider() );
		}
	}

	@RunWith( Suite.class )
	@SuiteClasses( AllTests.class )
	static public class JerseyProxy
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new JerseyProxyProvider() );
		}
	}

	@RunWith( Suite.class )
	@SuiteClasses( AllTests.class )
	static public class ResteasyBridge
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new ResteasyBridgeProvider() );
		}
	}

	@RunWith( Suite.class )
	@SuiteClasses( AllTests.class )
	static public class ResteasyProxy
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new ResteasyProxyProvider() );
		}
	}

}
