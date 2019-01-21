
package ascelion.rest.bridge.tests;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

public abstract class AbortTestCase<T> extends AbstractTestCase<T>
{

	static class ThrowException implements ClientResponseFilter
	{

		@Override
		public void filter( ClientRequestContext requestContext, ClientResponseContext responseContext ) throws IOException
		{
			throw new WebApplicationException( 555 );
		}
	}

	@Override
	public void setUp() throws Exception
	{
		final ClientBuilder bld = TestClientProvider.getInstance().getBuilder();

		bld.register( new ThrowException() );

		super.setUp();
	}
}
