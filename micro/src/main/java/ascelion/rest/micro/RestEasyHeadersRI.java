
package ascelion.rest.micro;

import javax.ws.rs.core.HttpHeaders;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestRequestContext;
import ascelion.rest.bridge.client.RestRequestInterceptor;
import ascelion.utils.chain.InterceptorChainContext;

import static ascelion.utils.etc.Secured.runPrivilegedWithException;

final class RestEasyHeadersRI implements RestRequestInterceptor
{

	static final Class<?> RPF = RBUtils.safeLoadClass( "org.jboss.resteasy.spi.ResteasyProviderFactory" );

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		runPrivilegedWithException( this::pushContext );

		try {
			return context.proceed();
		}
		finally {
			runPrivilegedWithException( this::popContext );
		}
	}

	@Override
	public int priority()
	{
		return PRIORITY_HEAD + 1;
	}

	private Object pushContext() throws Exception
	{
		return RPF.getMethod( "pushContext", Class.class, Object.class )
			.invoke( null, HttpHeaders.class, ThreadLocalProxy.create( HttpHeaders.class ) );
	}

	private Object popContext() throws Exception
	{
		return RPF.getMethod( "popContextData", Class.class )
			.invoke( null, HttpHeaders.class );
	}

}
