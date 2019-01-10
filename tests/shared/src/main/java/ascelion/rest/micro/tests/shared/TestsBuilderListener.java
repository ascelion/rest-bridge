
package ascelion.rest.micro.tests.shared;

import ascelion.rest.bridge.etc.RestClientTrace;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class TestsBuilderListener implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.register( new RestClientTrace(), Integer.MIN_VALUE );
	}

}
