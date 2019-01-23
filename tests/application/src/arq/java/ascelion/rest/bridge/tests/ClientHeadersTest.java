
package ascelion.rest.bridge.tests;

import ascelion.rest.bridge.tests.api.API;
import ascelion.rest.bridge.tests.app.ClientHeaders;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.Test;

public class ClientHeadersTest
extends Deployments
{

	@Test
	public void run()
	{
		final ClientHeaders client = RestClientBuilder.newBuilder()
			.baseUri( this.target.resolve( API.BASE ) )
			.build( ClientHeaders.class );

		assertThat( client.getHeaderValue(), equalTo( "VVVV" ) );
	}

}
