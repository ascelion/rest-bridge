
package ascelion.rest.bridge.client;

import java.net.URI;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import lombok.Setter;

public final class RestClient
{

	static public final String INSTANTIATOR_PROPERTY = "ascelion.rest.bridge.client.instantiator";

	final Client client;
	final ConvertersFactory cvsf;
	@Setter
	private URI target;

	public RestClient( Client client, URI target )
	{
		this( client, target, (String) null );
	}

	public RestClient( Client client, URI target, String base )
	{
		this.client = client;

		if( base == null || base.isEmpty() ) {
			this.target = target;
		}
		else {
			this.target = UriBuilder.fromUri( target ).path( base ).build();
		}

		this.cvsf = new ConvertersFactory( client.getConfiguration() );
	}

	public <X> X getInterface( Class<X> cls )
	{
		final Supplier<WebTarget> sup = () -> Util.addPathFromAnnotation( cls, this.client.target( this.target ) );
		final RestClientIH ih = new RestClientIH( cls, this.cvsf, sup );

		return ih.newProxy();
	}
}
