
package ascelion.rest.bridge.tests.app;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@RegisterClientHeaders( ClientHeaders.Factory.class )
@Path( "forward-headers" )
@ClientHeaderParam( name = ClientHeaders.NAME, value = ClientHeaders.VALUE )
public interface ClientHeaders
{

	String NAME = "H";
	String VALUE = "V";

	class Factory implements ClientHeadersFactory
	{

		static volatile int next;

		@Context
		private HttpHeaders headers;

		@Override
		public MultivaluedMap<String, String> update( MultivaluedMap<String, String> ih, MultivaluedMap<String, String> oh )
		{
			final MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
			final StringBuilder sb = new StringBuilder();

			ofNullable( ih.get( NAME ) ).ifPresent( h -> h.forEach( sb::append ) );
			ofNullable( headers.getRequestHeaders().get( NAME ) ).ifPresent( h -> h.forEach( sb::append ) );

			result.putSingle( NAME, trimToNull( sb.toString() ) );

			return result;
		}
	}

	@GET
	@Produces( MediaType.TEXT_PLAIN )
	String getHeaderValue();
}
