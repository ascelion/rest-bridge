
package ascelion.rest.micro.cdi;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Interceptor;

import ascelion.rest.bridge.client.RestRequestContext;
import ascelion.rest.bridge.client.RestRequestInterceptor;
import ascelion.utils.chain.InterceptorChainContext;

import static java.util.stream.Collectors.joining;
import static javax.enterprise.inject.spi.InterceptionType.AROUND_INVOKE;

final class CDIAroundInterceptor implements RestRequestInterceptor
{

	private final Interceptor<Object> itc;
	private final CreationalContext<Object> ccx;

	CDIAroundInterceptor( Interceptor<?> interceptor, CreationalContext<?> ccx )
	{
		this.itc = (Interceptor<Object>) interceptor;
		this.ccx = (CreationalContext<Object>) ccx;
	}

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		return this.itc.intercept( AROUND_INVOKE,
			this.itc.create( this.ccx ),
			new CDIAroundInvocation( context ) );
	}

	@Override
	public String about()
	{
		return this.itc.getInterceptorBindings().stream()
			.map( Annotation::annotationType )
			.map( Class::getSimpleName )
			.collect( joining( ", ", "CDI(", ")" ) );
	}

	@Override
	public int priority()
	{
		return PRIORITY_HEAD;
	}
}
