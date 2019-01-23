
package ascelion.rest.micro.tck;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;
import org.glassfish.jersey.client.JerseyClientBuilder;

public class JAXRSClientProvider implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder bld )
	{
		bld.property( ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, new JerseyClientBuilder() );
	}

}
