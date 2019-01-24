
package ascelion.rest.micro.tests.shared;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.etc.RestClientTrace;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class TestsBuilderListener implements RestClientBuilderListener
{

	static class ContentTypeResponseFilter implements ClientResponseFilter
	{

		@Override
		public void filter( ClientRequestContext req, ClientResponseContext rsp ) throws IOException
		{
			if( rsp.getStatus() == Response.Status.NO_CONTENT.getStatusCode() ) {
				return;
			}

			final Method m = RestClient.invokedMethod();

			if( m != null && rsp.getMediaType() == null && rsp.getStatus() != Response.Status.NO_CONTENT.getStatusCode() ) {
				final String type = RBUtils.genericType( m.getDeclaringClass(), m )
					.getRawType()
					.getSimpleName()
					.toLowerCase();

				switch( type ) {
					case "response":
					case "void":
					break;

					case "string":
						rsp.getHeaders().add( HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN );
					break;

					default:
						rsp.getHeaders().add( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON );
				}
			}
		}
	}

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.register( new ContentTypeResponseFilter(), Integer.MAX_VALUE );
		b.register( new RestClientTrace(), Integer.MAX_VALUE - 1 );
		b.register( new WildcardBodyWriter<>(), Integer.MAX_VALUE );
	}

}
