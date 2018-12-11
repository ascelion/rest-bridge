
package ascelion.rest.micro;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;

public class Resolver extends RestClientBuilderResolver
{

	@Override
	public RestClientBuilder newBuilder()
	{
		return new Builder( ClientBuilder.newClient() );
	}
}
