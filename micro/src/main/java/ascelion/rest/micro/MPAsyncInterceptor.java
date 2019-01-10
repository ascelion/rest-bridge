
package ascelion.rest.micro;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.client.AsyncInterceptor;
import ascelion.rest.bridge.client.Util;

import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;

@RequiredArgsConstructor
final class MPAsyncInterceptor implements AsyncInterceptor<Collection<AsyncInvocationInterceptor>>
{

	private final Configuration cf;

	@Override
	public Collection<AsyncInvocationInterceptor> prepare()
	{
		final List<AsyncInvocationInterceptor> aiis = Util.providers( this.cf, AsyncInvocationInterceptorFactory.class )
			.stream()
			.map( AsyncInvocationInterceptorFactory::newInterceptor )
			.collect( toList() );

		aiis.forEach( AsyncInvocationInterceptor::prepareContext );

		return aiis;
	}

	@Override
	public void before( Collection<AsyncInvocationInterceptor> aiis )
	{
		aiis.forEach( AsyncInvocationInterceptor::applyContext );
	}

	@Override
	public void after( Collection<AsyncInvocationInterceptor> aiis )
	{
		aiis.forEach( AsyncInvocationInterceptor::removeContext );
	}
}
