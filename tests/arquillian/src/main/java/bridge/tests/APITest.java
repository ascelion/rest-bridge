
package bridge.tests;

import ascelion.rest.bridge.web.BeanAPI;
import ascelion.rest.bridge.web.BeanData;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import bridge.tests.arquillian.IgnoreWithProvider;
import bridge.tests.providers.ResteasyBridgeProvider;
import bridge.tests.providers.ResteasyProxyProvider;
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
