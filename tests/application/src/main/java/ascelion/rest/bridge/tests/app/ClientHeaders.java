
package ascelion.rest.bridge.tests.app;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@RegisterClientHeaders( ClientHeaders.Factory.class )
@Path( "forward-headers" )
@ClientHeaderParam( name = "H1", value = "V1" )
public interface ClientHeaders
{

	class Factory implements ClientHeadersFactory
	{

		@Override
		public MultivaluedMap<String, String> update( MultivaluedMap<String, String> ih, MultivaluedMap<String, String> oh )
		{
			final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			final String h1 = ih.getFirst( "H1" );

			if( h1 != null ) {
				headers.putSingle( "H1", "Forward: " + h1 );
			}

			return headers;
		}
	}

	@GET
	@Produces( MediaType.TEXT_PLAIN )
	String getHeaderValue();
}
