
package ascelion.rest.bridge.tests;

import ascelion.rest.bridge.tests.providers.JerseyBridgeProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;
import ascelion.rest.bridge.tests.providers.ResteasyBridgeProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
	ProvidersSuite.JerseyBridge.class,
	ProvidersSuite.JerseyProxy.class,
	ProvidersSuite.ResteasyBridge.class,
	ProvidersSuite.ResteasyProxy.class,
} )
public class ProvidersSuite
{

	@RunWith( Suite.class )
	@SuiteClasses( TestsSuite.class )
	static public class JerseyBridge
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new JerseyBridgeProvider() );
		}
	}

	@RunWith( Suite.class )
	@SuiteClasses( TestsSuite.class )
	static public class JerseyProxy
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new JerseyProxyProvider() );
		}
	}

	@RunWith( Suite.class )
	@SuiteClasses( TestsSuite.class )
	static public class ResteasyBridge
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new ResteasyBridgeProvider() );
		}
	}

	@RunWith( Suite.class )
	@SuiteClasses( TestsSuite.class )
	static public class ResteasyProxy
	{

		@BeforeClass
		static public void setUpClass()
		{
			TestClientProvider.setInstance( new ResteasyProxyProvider() );
		}
	}

}
