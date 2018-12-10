
package ascelion.rest.bridge.tests;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ascelion.rest.bridge.tests.api.API;
import ascelion.rest.bridge.tests.api.BeanParamData;
import ascelion.rest.bridge.tests.api.BeanResource;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BeanResourceTest
extends AbstractTestCase<BeanResource>
{

	static private void assertValid( final BeanParamData ent )
	{
		assertNotNull( ent );

		assertEquals( "path1", ent.getPathParam1() );
		assertEquals( "path2", ent.getPathParam2() );
		assertEquals( "query1", ent.getQueryParam1() );
		assertEquals( "query2", ent.getQueryParam2() );
		assertEquals( "header1", ent.getHeaderParam1() );
		assertEquals( "header2", ent.getHeaderParam2() );
	}

	static private BeanParamData createBean()
	{
		final BeanParamData request = new BeanParamData( "path2", "header2", "query2" );

		request.setPathParam1( "path1" );
		request.setHeaderParam1( "header1" );
		request.setQueryParam1( "query1" );

		return request;
	}

	@Test
	@IgnoreWithProvider( JerseyProxyProvider.class )
	public void get()
	{
		assertValid( this.client.get( createBean() ) );
	}

	@Test
	public void getByClient()
	{
		final WebTarget w = TestClientProvider.getInstance()
			.getBuilder()
			.build()
			.target( this.target )
			.path( API.BASE )
			.path( "beans" )
			.path( "{path1}/{path2}" )
			.resolveTemplate( "path1", "path1" )
			.resolveTemplate( "path2", "path2" )
			.queryParam( "query1", "query1" )
			.queryParam( "query2", "query2" )

		;

		final Builder b = w.request( MediaType.APPLICATION_JSON );

		b.header( "header1", "header1" );
		b.header( "header2", "header2" );

		final Response resp = b.method( "GET" );

		assertNotNull( resp );

		assertEquals( Response.Status.OK.getStatusCode(), resp.getStatus() );

		assertValid( resp.readEntity( BeanParamData.class ) );
	}
}
