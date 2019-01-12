
package ascelion.rest.bridge.tests;

import javax.ws.rs.WebApplicationException;

import ascelion.rest.bridge.tests.api.AsyncBeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AbortAsyncAPITest
extends AbortTestCase<AsyncBeanAPI>
{

	@Override
	public void setUp() throws Exception
	{
		TestClientProvider.getInstance().getBuilder().register( AsyncThreadFilter.class );

		AsyncThreadFilter.reset();

		super.setUp();
	}

	@Override
	public void tearDown()
	{
		AsyncThreadFilter.checkThread();

		super.tearDown();
	}

	@Test( expected = WebApplicationException.class )
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void create()
	{
		try {
			this.client.create( new BeanData( "create" ) ).toCompletableFuture().get();
		}
		catch( final Exception e ) {
			checkException( e );
		}
	}

	@Test( expected = WebApplicationException.class )
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void get()
	{
		try {
			this.client.get().toCompletableFuture().get();
		}
		catch( final Exception e ) {
			checkException( e );
		}
	}

	@Test( expected = WebApplicationException.class )
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void update()
	{
		try {
			this.client.update( new BeanData( "update" ) ).toCompletableFuture().get();
		}
		catch( final Exception e ) {
			checkException( e );
		}
	}

	@Test( expected = WebApplicationException.class )
	public void delete()
	{
		try {
			this.client.delete().toCompletableFuture().get();
		}
		catch( final Exception e ) {
			checkException( e );
		}
	}

	private void checkException( Exception e )
	{
		final Throwable t = e.getCause();

		assertThat( t, instanceOf( WebApplicationException.class ) );

		final WebApplicationException w = (WebApplicationException) t;

		assertThat( w.getResponse().getStatus(), equalTo( 555 ) );

		throw w;
	}
}
