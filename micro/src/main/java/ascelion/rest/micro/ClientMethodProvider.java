
package ascelion.rest.micro;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import ascelion.rest.bridge.client.RestClient;

final class ClientMethodProvider implements ClientRequestFilter
{

	@Override
	public void filter( ClientRequestContext cx ) throws IOException
	{
		cx.setProperty( "org.eclipse.microprofile.rest.client.invokedMethod", RestClient.invokedMethod() );
	}
}
