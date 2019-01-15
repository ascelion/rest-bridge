
package ascelion.rest.micro;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import ascelion.rest.bridge.client.ResponseHandler;
import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.client.Util;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

final class MPResponseHandler implements ResponseHandler
{

	static private final String CONFIG_KEY_DISABLE_DEFAULT_MAPPER = "microprofile.rest.client.disable.default.mapper";

	private final Collection<ResponseExceptionMapper> providers;

	MPResponseHandler( Configuration cf )
	{
		this.providers = Util.providers( cf, ResponseExceptionMapper.class );

		final String dis = MP.getConfig( CONFIG_KEY_DISABLE_DEFAULT_MAPPER )
			.orElse( Objects.toString( cf.getProperty( CONFIG_KEY_DISABLE_DEFAULT_MAPPER ), "false" ) );

		if( !Boolean.valueOf( dis ) ) {
			this.providers.add( rsp -> new WebApplicationException( rsp ) );
		}
	}

	@Override
	public void handleResponse( Response rsp ) throws Exception
	{
		final Throwable ex = this.providers
			.stream()
			.filter( h -> h.handles( rsp.getStatus(), rsp.getHeaders() ) )
			.map( h -> h.toThrowable( rsp ) )
			.filter( Objects::nonNull )
			.filter( this::matchesMethod )
			.findFirst()
			.orElse( null );

		if( ex == null ) {
			return;
		}

		if( ex instanceof Error ) {
			throw(Error) ex;
		}

		throw(Exception) ex;
	}

	private boolean matchesMethod( final Throwable ex )
	{
		if( ex instanceof Error ) {
			return true;
		}
		if( ex instanceof RuntimeException ) {
			return true;
		}

		final Method m = RestClient.invokedMethod();

		return Stream.of( m.getExceptionTypes() )
			.anyMatch( t -> t.isInstance( ex ) );
	}

}
