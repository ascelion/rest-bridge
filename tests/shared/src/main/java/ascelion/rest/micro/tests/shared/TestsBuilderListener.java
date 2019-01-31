
package ascelion.rest.micro.tests.shared;

import ascelion.utils.jaxrs.RestClientTrace;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class TestsBuilderListener implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.register( new ContentTypeResponseFilter(), Integer.MAX_VALUE );
		b.register( new RestClientTrace(), Integer.MAX_VALUE - 1 );
		b.register( new WildcardBodyRW(), Integer.MAX_VALUE );
	}

}
