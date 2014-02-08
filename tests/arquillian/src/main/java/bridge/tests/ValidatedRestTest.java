
package bridge.tests;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;

import ascelion.rest.bridge.web.BeanData;
import ascelion.rest.bridge.web.RestApplication;
import ascelion.rest.bridge.web.Validated;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ValidatedRestTest
extends Deployments
{

	@ArquillianResource
	private URI target;

	@Test
	public void bean_WithInvalid()
	{
		beanCall( "bean", new BeanData(), OK );
	}

	@Test
	public void bean_WithNull()
	{
		beanCall( "bean", null, NO_CONTENT );
	}

	@Test
	public void bean_WithValid()
	{
		beanCall( "bean", new BeanData( "gigel" ), OK );
	}

	@Test
	public void beanNotNull_WithInvalid()
	{
		beanCall( "beanNotNull", new BeanData(), OK );
	}

	@Test
	public void beanNotNull_WithNull()
	{
		beanCall( "beanNotNull", null, BAD_REQUEST );
	}

	@Test
	public void beanNotNull_WithValid()
	{
		beanCall( "beanNotNull", new BeanData( "gigiel" ), OK );
	}

	@Test
	public void beanValid_WithInvalid()
	{
		beanCall( "beanValid", new BeanData(), BAD_REQUEST );
	}

	@Test
	public void beanValid_WithNull()
	{
		beanCall( "beanValid", null, NO_CONTENT );
	}

	@Test
	public void beanValid_WithValid()
	{
		beanCall( "beanValid", new BeanData( "gigel" ), OK );
	}

	@Test
	public void beanValidNotNull_WithInvalid()
	{
		beanCall( "beanValidNotNull", new BeanData(), BAD_REQUEST );
	}

	@Test
	public void beanValidNotNull_WithNull()
	{
		beanCall( "beanValidNotNull", null, BAD_REQUEST );
	}

	@Test
	public void beanValidNotNull_WithValid()
	{
		beanCall( "beanValidNotNull", new BeanData( "gigel" ), OK );
	}

	@Test
	public void notNullFormParam_WithNotNull()
	{
		notNullFormParam( "gigel", OK );
	}

	@Test
	public void notNullFormParam_WithNull()
	{
		notNullFormParam( null, BAD_REQUEST );
	}

	@Test
	public void notNullHeaderParam_WithNotNull()
	{
		notNullHeaderParam( "gigel", OK );
	}

	@Test
	public void notNullHeaderParam_WithNull()
	{
		notNullHeaderParam( null, BAD_REQUEST );
	}

	@Test
	public void notNullQueryParam_BAD()
	{
		notNullQueryParam( null, BAD_REQUEST );
	}

	@Test
	public void notNullQueryParam_OK()
	{
		notNullQueryParam( "gigel", OK );
	}

	private void beanCall( String path, final BeanData v, Status status )
	{
		final WebTarget w = getTarget( path );
		final Builder b = w.request( MediaType.APPLICATION_JSON );
		final Entity e = v != null ? Entity.entity( v, MediaType.APPLICATION_JSON ) : null;
		final Response r = b.method( "POST", e );

		assertNotNull( r );
		assertEquals( status.getStatusCode(), r.getStatus() );

		if( status == OK ) {
			assertEquals( v, r.readEntity( BeanData.class ) );
		}
	}

	private WebTarget getTarget( String path )
	{
		return ClientBuilder.newClient()
			.target( this.target )
			.path( RestApplication.BASE )
			.path( Validated.PATH )
			.path( path );
	}

	private void notNullFormParam( final String v, Status status )
	{
		final WebTarget w = getTarget( "notNullFormParam" );
		final Builder b = w.request( MediaType.APPLICATION_FORM_URLENCODED );

		final Form f = new Form();

		f.param( "value", v );

		final Response r = b.method( "POST", Entity.entity( f, MediaType.APPLICATION_FORM_URLENCODED ) );

		assertNotNull( r );
		assertEquals( status.getStatusCode(), r.getStatus() );

		if( status == OK ) {
			assertEquals( v, r.readEntity( String.class ) );
		}
	}

	private void notNullHeaderParam( final String v, Status status )
	{
		final WebTarget w = getTarget( "notNullHeaderParam" );
		final Builder b = w.request().header( "value", v );
		final Response r = b.method( "GET" );

		assertNotNull( r );
		assertEquals( status.getStatusCode(), r.getStatus() );

		if( status == OK ) {
			assertEquals( v, r.readEntity( String.class ) );
		}
	}

	private void notNullQueryParam( final String v, Status status )
	{
		final WebTarget w = getTarget( "notNullQueryParam" ).queryParam( "value", v );
		final Builder b = w.request();
		final Response r = b.method( "GET" );

		assertNotNull( r );
		assertEquals( status.getStatusCode(), r.getStatus() );

		if( status == OK ) {
			assertEquals( v, r.readEntity( String.class ) );
		}
	}
}
