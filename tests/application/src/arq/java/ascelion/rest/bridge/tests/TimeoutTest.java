
package ascelion.rest.bridge.tests;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.tests.api.BeanAPI;

import static ascelion.rest.bridge.client.RestClient.newRestClient;

import org.junit.Test;

public class TimeoutTest
{

	@Test( expected = ProcessingException.class )
	public void timeout()
	{
		final Client client = TestClientProvider.getInstance().getBuilder()
			.connectTimeout( 5, TimeUnit.SECONDS )
			.readTimeout( 5, TimeUnit.SECONDS )
			.build();

		final RestClient rc = newRestClient( client, URI.create( "http://ascelion.com:1234" ) );
		final BeanAPI api = rc.getInterface( BeanAPI.class );

		api.get();
	}
}
