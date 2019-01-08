
package ascelion.rest.micro.tests.shared;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class TestsBuilderListener implements RestClientBuilderListener
{

	static final String DEFAULT_CONTENT_TYPE = "ascelion.rest.bridge.defaultContentType";

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.property( DEFAULT_CONTENT_TYPE, "application/json" );
		b.register( new RestClientTrace(), Integer.MIN_VALUE );
	}

}
