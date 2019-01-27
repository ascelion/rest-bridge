
package ascelion.rest.bridge.client;

import javax.ws.rs.Path;

import lombok.Getter;

@Getter
public class RestServiceInfo extends RestClientInfoImpl
{

	private final Class<?> serviceType;
	private final String serviceURI;

	RestServiceInfo( RestServiceInfo rsi )
	{
		super( rsi );

		this.serviceType = rsi.serviceType;
		this.serviceURI = RBUtils.getRequestURI( this.serviceType.getAnnotation( Path.class ) );
	}

	RestServiceInfo( RestClientInfo rci, Class<?> serviceType )
	{
		super( rci );

		this.serviceType = serviceType;
		this.serviceURI = RBUtils.getRequestURI( this.serviceType.getAnnotation( Path.class ) );
	}

}
