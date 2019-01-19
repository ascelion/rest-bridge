
package ascelion.rest.bridge.tests;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class JerseyTrace implements ClientRequestFilter
{

	@Override
	public void filter( ClientRequestContext cx ) throws IOException
	{
		if( Boolean.getBoolean( "jersey.trace" ) ) {
			cx.getHeaders().add( "X-Jersey-Tracing-Accept", "true" );
		}
	}

}
