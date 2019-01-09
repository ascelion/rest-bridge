
package ascelion.rest.micro.tests.shared;

import ascelion.rest.bridge.etc.RestClientTrace;

import static ascelion.rest.bridge.client.RestClientProperties.DEFAULT_CONTENT_TYPE;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class TestsBuilderListener implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.property( DEFAULT_CONTENT_TYPE, "application/json" );
		b.register( new RestClientTrace(), Integer.MIN_VALUE );
	}

}
