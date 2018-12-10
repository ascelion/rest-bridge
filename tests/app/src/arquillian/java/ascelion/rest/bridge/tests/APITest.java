
package ascelion.rest.bridge.tests;

import ascelion.rest.bridge.tests.api.BeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.ResteasyBridgeProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class APITest
extends AbstractTestCase<BeanAPI>
{

	@Test
	@IgnoreWithProvider( {
		ResteasyBridgeProvider.class,
		ResteasyProxyProvider.class,
	} )
	public void create()
	{
		final BeanData b0 = new BeanData( "cici" );
		final BeanData b1 = this.client.create( b0 );

		assertThat( b1, is( equalTo( b0 ) ) );
	}

	@Test
	@IgnoreWithProvider( {
		ResteasyBridgeProvider.class,
		ResteasyProxyProvider.class,
	} )
	public void get()
	{
		final BeanData b0 = new BeanData( "value" );
		final BeanData b1 = this.client.get();

		assertThat( b1, is( equalTo( b0 ) ) );
	}
}
