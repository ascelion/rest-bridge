
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
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BeanResourceTest
extends AbstractTestCase<BeanResource>
{

	static private void assertValid( final BeanParamData ent )
	{
		assertNotNull( ent );

		assertEquals( "path1Val", ent.getPathParam1() );
		assertEquals( "path2Val", ent.getPathParam2() );
		assertEquals( "query1Val", ent.getQueryParam1() );
		assertEquals( "query2Val", ent.getQueryParam2() );
		assertEquals( "header1Val", ent.getHeaderParam1() );
		assertEquals( "header2Val", ent.getHeaderParam2() );
	}

	static private BeanParamData createBean()
	{
		final BeanParamData request = new BeanParamData( "path2Val", "header2Val", "query2Val" );

		request.setPathParam1( "path1Val" );
		request.setHeaderParam1( "header1Val" );
		request.setQueryParam1( "query1Val" );

		return request;
	}

	@Test
	@IgnoreWithProvider( JerseyProxyProvider.class )
	@IgnoreWithProvider( value = ResteasyProxyProvider.class, reason = "bad accept header" )
	public void get()
	{
		final BeanParamData bean = this.client.get( createBean() );

		assertValid( bean );
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
			.resolveTemplate( "path1", "path1Val" )
			.resolveTemplate( "path2", "path2Val" )
			.queryParam( "query1", "query1Val" )
			.queryParam( "query2", "query2Val" )

		;

		final Builder b = w.request( MediaType.APPLICATION_JSON );

		b.header( "header1", "header1Val" );
		b.header( "header2", "header2Val" );

		final Response resp = b.method( "GET" );

		assertNotNull( resp );

		assertEquals( Response.Status.OK.getStatusCode(), resp.getStatus() );

		assertValid( resp.readEntity( BeanParamData.class ) );
	}
}
