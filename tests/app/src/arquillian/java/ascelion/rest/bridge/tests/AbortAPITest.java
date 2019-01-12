
package ascelion.rest.bridge.tests;

import javax.ws.rs.WebApplicationException;

import ascelion.rest.bridge.tests.api.BeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AbortAPITest
extends AbortTestCase<BeanAPI>
{

	@Test( expected = WebApplicationException.class )
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void create()
	{
		try {
			this.client.create( new BeanData( "create" ) );
		}
		catch( final WebApplicationException e ) {
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
			this.client.get();
		}
		catch( final WebApplicationException e ) {
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
			this.client.update( new BeanData( "update" ) );
		}
		catch( final WebApplicationException e ) {
			checkException( e );
		}
	}

	@Test( expected = WebApplicationException.class )
	public void delete()
	{
		try {
			this.client.delete();
		}
		catch( final WebApplicationException e ) {
			checkException( e );
		}
	}

	private void checkException( WebApplicationException e )
	{
		assertThat( e.getResponse().getStatus(), equalTo( 555 ) );

		throw e;
	}
}
