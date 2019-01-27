
package ascelion.rest.micro;

import java.util.ArrayList;
import java.util.Collection;

import ascelion.rest.bridge.client.RestMethodInfo;
import ascelion.rest.bridge.client.RestRequestInterceptor;

final class RRIFactory implements RestRequestInterceptor.Factory
{

	static Collection<RestRequestInterceptor> GLOBAL = new ArrayList<>();

	static {
		GLOBAL.add( new HeadersProxyRI() );

		if( RestEasyHeadersRI.RPF != null ) {
			GLOBAL.add( new RestEasyHeadersRI() );
		}
	}

	@Override
	public Iterable<RestRequestInterceptor> create( RestMethodInfo mi )
	{
		final Collection<RestRequestInterceptor> interceptors = new ArrayList<>( GLOBAL );

		interceptors.add( new ClientHeadersRI( mi.getServiceType(), mi.getJavaMethod() ) );
		interceptors.add( new ClientHeadersFactoryRI( mi.getServiceType(), mi.getJavaMethod() ) );

		return interceptors;
	}

}
