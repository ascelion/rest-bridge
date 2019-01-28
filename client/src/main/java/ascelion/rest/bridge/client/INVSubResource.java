
package ascelion.rest.bridge.client;

import ascelion.utils.chain.InterceptorChainContext;

import static java.lang.String.format;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class INVSubResource implements RestRequestInterceptor
{

	private final RestClientInternals rci;
	private final Class<?> resourceType;

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = context.getData();
		final RestMethodInfo mi = rc.getMethodInfo();
		final RestClientInfo rci = new RestClientInfoImpl( mi, () -> rc.getReqTarget() );
		final RestServiceInfo rsi = new RestServiceInfo( rci, this.resourceType );

		try {
			return new RestService( rsi, this.rci ).newProxy();
		}
		catch( final RestClientException e ) {
			final Throwable c = e.getCause();

			if( c == null ) {
				throw e;
			}

			throw new RestClientException( format( "Cannot create subresource \"%s\": %s", mi, e.getMessage() ), e );
		}
	}

	@Override
	public int priority()
	{
		return PRIORITY_INVOCATION;
	}

}
