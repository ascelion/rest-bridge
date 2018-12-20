
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import lombok.Setter;

public final class RestClient
{

	static final ThreadLocal<Method> METHOD = new ThreadLocal<>();

	static public Method invokedMethod()
	{
		return METHOD.get();
	}

	private final Client client;
	@Setter
	private URI target;
	private final ConvertersFactory cvsf;
	private ResponseHandler rsph = ResponseHandler.NONE;

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

	public <X> X getInterface( Class<X> type )
	{
		final Supplier<WebTarget> sup = () -> Util.addPathFromAnnotation( type, this.client.target( this.target ) );
		final RestBridgeType rbt = new RestBridgeType( type, this.client.getConfiguration(), this.cvsf, this.rsph, sup );
		final RestClientIH ih = new RestClientIH( rbt );

		return ih.newProxy();
	}

	public void setResponseHandler( ResponseHandler rsph )
	{
		this.rsph = rsph;
	}
}
