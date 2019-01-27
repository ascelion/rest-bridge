
package ascelion.rest.micro;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestRequestContext;
import ascelion.rest.bridge.client.RestRequestInterceptorBase;
import ascelion.utils.etc.TypeDescriptor;

import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

final class ClientHeadersFactoryRI extends RestRequestInterceptorBase
{

	private final ThreadLocalValue<HttpHeaders> headers = ThreadLocalProxy.create( HttpHeaders.class );
	private final Collection<Consumer<RestRequestContext>> actions = new ArrayList<>();

	ClientHeadersFactoryRI( Class<?> type, Method method )
	{
		final List<Class<?>> all = getAllInterfaces( type );

		all.add( 0, type );

		all.stream()
			.map( t -> t.getAnnotation( RegisterClientHeaders.class ) )
			.filter( Objects::nonNull )
			.forEach( a -> this.actions.add( rc -> headersFactory( rc, a ) ) );
	}

	@Override
	public int priority()
	{
		return PRIORITY_PARAMETERS + 1;
	}

	@Override
	protected void before( RestRequestContext rc )
	{
		this.actions.forEach( a -> a.accept( rc ) );
	}

	private void headersFactory( RestRequestContext rc, RegisterClientHeaders a )
	{
		final MultivaluedMap<String, String> incHeaders = this.headers.get().getRequestHeaders();
		final ClientHeadersFactory factory = RBUtils.newInstance( a.value() );
		final TypeDescriptor td = new TypeDescriptor( factory );

		td.injectAnnotated( Context.class, HttpHeaders.class, factory, this.headers );

		final MultivaluedMap<String, String> headers = factory
			.update( incHeaders, rc.getHeaders() );

		rc.getHeaders().putAll( headers );
	}

}
