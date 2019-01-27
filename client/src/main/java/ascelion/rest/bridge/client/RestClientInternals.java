
package ascelion.rest.bridge.client;

import java.util.Collection;

interface RestClientInternals
{

	Collection<RestRequestInterceptor.Factory> rriFactories();
}
