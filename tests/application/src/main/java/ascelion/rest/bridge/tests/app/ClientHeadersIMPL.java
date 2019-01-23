
package ascelion.rest.bridge.tests.app;

import java.io.IOException;
import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

@RequestScoped
public class ClientHeadersIMPL implements ClientHeaders
{

	static public class ClientHeaderFilter implements ClientRequestFilter
	{

		@Context
		private HttpHeaders headers;

		@Override
		public void filter( ClientRequestContext rc ) throws IOException
		{
			final Response rsp = Response.ok()
				.entity( rc.getHeaderString( NAME ) )
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
