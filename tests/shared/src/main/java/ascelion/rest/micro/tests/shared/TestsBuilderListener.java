
package ascelion.rest.micro.tests.shared;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import ascelion.rest.bridge.client.RestClientProperties;
import ascelion.rest.bridge.etc.RestClientTrace;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class TestsBuilderListener implements RestClientBuilderListener
{

	static class ContentTypeFilter implements ClientResponseFilter
	{

		@Override
		public void filter( ClientRequestContext req, ClientResponseContext rsp ) throws IOException
		{
			if( rsp.getMediaType() == null ) {
				rsp.getHeaders().add( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON );
			}
		}
	}

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.register( new ContentTypeFilter(), Integer.MAX_VALUE );
		b.register( new RestClientTrace(), Integer.MAX_VALUE );

		b.property( RestClientProperties.DEFAULT_CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE );
	}

}
