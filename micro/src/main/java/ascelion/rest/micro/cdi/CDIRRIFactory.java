
package ascelion.rest.micro.cdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;

import ascelion.rest.bridge.client.RestMethodInfo;
import ascelion.rest.bridge.client.RestRequestInterceptor;

import static java.util.Arrays.stream;

public class CDIRRIFactory implements RestRequestInterceptor.Factory
{

	static final ThreadLocal<CreationalContext<?>> CCX = new ThreadLocal<>();

	@Inject
	private BeanManager bm;

	@Override
	public Iterable<RestRequestInterceptor> create( RestMethodInfo rmi )
	{
		Objects.requireNonNull( this.bm, "BM is not injected" );

		final Collection<RestRequestInterceptor> result = new ArrayList<>();

		final Annotation[] bindings = Stream.concat( getInterceptorBindings( rmi.getServiceType().getAnnotations() ),
			getInterceptorBindings( rmi.getJavaMethod().getAnnotations() ) )
			.toArray( Annotation[]::new );

		if( bindings.length > 0 ) {
			final List<Interceptor<?>> interceptors = this.bm.resolveInterceptors( InterceptionType.AROUND_INVOKE, bindings );

			interceptors.stream()
				.map( i -> new CDIAroundInterceptor( i, CCX.get() ) )
				.forEach( result::add );
		}

		return result;
	}

	private Stream<Annotation> getInterceptorBindings( Annotation[] annotations )
	{
		return stream( annotations )
			.filter( a -> this.bm.isInterceptorBinding( a.annotationType() ) );
	}

}
