
package ascelion.rest.bridge.client;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MockClient
{

	final Configuration configuration;
	final Client client;
	final WebTarget clientTarget;
	final WebTarget methodTarget;
	final Invocation.Builder bld;

	MockClient()
	{
		this.configuration = mock( Configuration.class, withSettings().lenient() );
		this.client = mock( Client.class, withSettings().lenient() );
		this.clientTarget = mock( WebTarget.class, withSettings().lenient() );
		this.methodTarget = mock( WebTarget.class, withSettings().lenient() );
		this.bld = mock( Invocation.Builder.class, withSettings().lenient() );

		when( this.client.getConfiguration() ).thenReturn( this.configuration );
		when( this.client.target( any( URI.class ) ) ).thenReturn( this.clientTarget );
		when( this.methodTarget.request() ).thenReturn( this.bld );
	}
}
