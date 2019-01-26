
package ascelion.rest.bridge.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.mockito.Answers;

class MockClient
{

	final Configuration configuration;
	final Client client;
	final WebTarget clientTarget;
	final WebTarget methodTarget;
	final Invocation.Builder bld;
	final Response rsp;

	MockClient()
	{
		this.configuration = mock( Configuration.class, withSettings().lenient().defaultAnswer( Answers.CALLS_REAL_METHODS ) );
		this.client = mock( Client.class, withSettings().lenient() );
		this.clientTarget = mock( WebTarget.class, withSettings().lenient() );
		this.methodTarget = mock( WebTarget.class, withSettings().lenient() );
		this.bld = mock( Invocation.Builder.class, withSettings().lenient() );
		this.rsp = mock( Response.class, withSettings().lenient() );

		when( this.client.getConfiguration() )
			.thenReturn( this.configuration );
		when( this.client.target( any( String.class ) ) )
			.thenReturn( this.methodTarget );
		when( this.methodTarget.path( any( String.class ) ) )
			.thenReturn( this.methodTarget );
		when( this.methodTarget.request() )
			.thenReturn( this.bld );
		when( this.bld.method( any( String.class ) ) )
			.thenReturn( this.rsp );
		when( this.bld.method( any( String.class ), any( Entity.class ) ) )
			.thenReturn( this.rsp );
	}
}
