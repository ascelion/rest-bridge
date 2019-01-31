
package ascelion.rest.micro.tests.shared;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestClient;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

final class ContentTypeResponseFilter implements ClientResponseFilter
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
					rsp.getHeaders().add( CONTENT_TYPE, TEXT_PLAIN );
				break;

				default:
					rsp.getHeaders().add( CONTENT_TYPE, APPLICATION_JSON );
			}
		}
	}
}
