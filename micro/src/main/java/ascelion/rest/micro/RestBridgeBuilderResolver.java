
package ascelion.rest.micro;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;

public class RestBridgeBuilderResolver extends RestClientBuilderResolver
{

	@Override
	public RestClientBuilder newBuilder()
	{
		return new RestBridgeBuilder();
	}
}
