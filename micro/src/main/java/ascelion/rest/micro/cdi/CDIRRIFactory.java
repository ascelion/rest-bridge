
package ascelion.rest.micro.cdi;

import ascelion.rest.bridge.client.RestMethodInfo;
import ascelion.rest.bridge.client.RestRequestInterceptor;

import static java.util.Collections.emptyList;

public class CDIRRIFactory implements RestRequestInterceptor.Factory
{

	@Override
	public Iterable<RestRequestInterceptor> create( RestMethodInfo rmi )
	{
		return emptyList();
	}

}
