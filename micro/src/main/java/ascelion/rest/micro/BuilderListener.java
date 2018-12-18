
package ascelion.rest.micro;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class BuilderListener implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder bld )
	{
		bld.register( new ClientMethodProvider(), Integer.MIN_VALUE );
	}

}
