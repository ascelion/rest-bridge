
package ascelion.rest.bridge.tests;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

public class BuilderConfigureListener implements RestClientListener
{

	@Override
	public void onNewClient( Class<?> type, RestClientBuilder bld )
	{
		bld.property( ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, TestClientProvider.getInstance().getBuilder() );
	}

}
