
package ascelion.rest.bridge.client;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import static ascelion.rest.bridge.client.RestClientProperties.NO_RESPONSE_HANDLER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MockClient
{

	interface RCI extends RestClientInfo, RestClientInternals
	{
	}

	interface CFG extends Configuration, Configurable<CFG>
	{

		@Override
		default Configuration getConfiguration()
		{
			return this;
		}
	}

	final RCI rci;
	final CFG configuration;
	final WebTarget target;
	final Invocation.Builder bld;
	final Response rsp;

	MockClient()
	{
		this.configuration = mock( CFG.class, withSettings().lenient() );

		when( this.configuration.getConfiguration() )
			.thenReturn( this.configuration );

		final ConvertersFactory cvsf = new ConvertersFactoryImpl( this.configuration );

		this.rci = mock( RCI.class, withSettings().lenient() );
		this.target = mock( WebTarget.class, withSettings().lenient() );
		this.bld = mock( Invocation.Builder.class, withSettings().lenient() );
		this.rsp = mock( Response.class, withSettings().lenient() );

		when( this.rci.getConfiguration() )
			.thenReturn( this.configuration );
		when( this.rci.getTarget() )
			.thenReturn( () -> this.target );
		when( this.rci.getResponseHandler() )
			.thenReturn( NO_RESPONSE_HANDLER );
		when( this.rci.getConvertersFactory() )
			.thenReturn( cvsf );

		final Collection<RestRequestInterceptor.Factory> rrif = new ArrayList<>();

		when( this.rci.rriFactories() )
			.thenReturn( rrif );

		when( this.target.path( any( String.class ) ) )
			.thenReturn( this.target );
		when( this.target.request() )
			.thenReturn( this.bld );
		when( this.bld.method( any( String.class ) ) )
			.thenReturn( this.rsp );
		when( this.bld.method( any( String.class ), any( Entity.class ) ) )
			.thenReturn( this.rsp );
	}
}
