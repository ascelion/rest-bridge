
package ascelion.rest.bridge.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class RestClientTest
{

	@Test
	public void create()
	throws URISyntaxException
	{
		final URI target = new URI( "http://localhost:8181" );

		final RestClient rc = new RestClient( target );

		final Interface ct = rc.getInterface( Interface.class );

		Assert.assertNotNull( ct );
	}
}
