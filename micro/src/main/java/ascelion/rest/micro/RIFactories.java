
package ascelion.rest.micro;

import java.lang.reflect.Method;

import ascelion.rest.bridge.client.RestRequestInterceptor;

public class RIFactories implements RestRequestInterceptor.Factory
{

	@Override
	public RestRequestInterceptor[] create( Class<?> type, Method method )
	{
		return new RestRequestInterceptor[] {
			new ClientHeadersRI( type, method ),
		};
	}

}
