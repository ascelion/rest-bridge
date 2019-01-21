
package ascelion.rest.bridge.tests.app;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ascelion.rest.bridge.etc.RestClientTrace;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

@RequestScoped
public class ClientHeadersIMPL implements ClientHeaders
{

	static private final Logger L = Logger.getLogger( "ClientHeadersTrace" );

	static public class ClientHeaderFilter implements ClientRequestFilter
	{

		@Override
		public void filter( ClientRequestContext rc ) throws IOException
		{
			final Response rsp = Response.ok()
				.entity( rc.getHeaderString( "H1" ) )
				.type( MediaType.TEXT_PLAIN )
				.build();

			rc.abortWith( rsp );
		}
	}

	@Override
	public String getHeaderValue()
	{
		try {
			final ClientHeaders ch = RestClientBuilder.newBuilder()
				.baseUri( URI.create( "http://localhost:1111" ) )
				.register( new RestClientTrace( L, Level.INFO ) )
				.register( ClientHeaderFilter.class )
				.build( ClientHeaders.class );
			;

			return ch.getHeaderValue();
		}
		catch( final Throwable t ) {
			t.printStackTrace();

			return null;
		}
	}
}
