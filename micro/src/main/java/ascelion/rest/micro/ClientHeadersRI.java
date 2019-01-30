
package ascelion.rest.micro;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestMethodInfo;
import ascelion.rest.bridge.client.RestRequestContext;
import ascelion.rest.bridge.client.RestRequestInterceptorBase;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

final class ClientHeadersRI extends RestRequestInterceptorBase
{

	static class EvalException extends RuntimeException
	{

		EvalException( Throwable cause )
		{
			super( cause );
		}
	}

	private final Collection<Consumer<RestRequestContext>> actions = new ArrayList<>();

	ClientHeadersRI( Class<?> type, Method method )
	{
		final List<Class<?>> all = getAllInterfaces( type );

		all.add( 0, type );

		all.stream()
			.flatMap( t -> stream( t.getAnnotationsByType( ClientHeaderParam.class ) ) )
			.forEach( a -> this.actions.add( rc -> setHeader( rc, a ) ) );
		stream( method.getAnnotationsByType( ClientHeaderParam.class ) )
			.forEach( a -> this.actions.add( rc -> setHeader( rc, a ) ) );
	}

	@Override
	public int priority()
	{
		return PRIORITY_PARAMETERS - 1;
	}

	@Override
	protected void before( RestRequestContext rc )
	{
		this.actions.forEach( a -> a.accept( rc ) );
	}

	private void setHeader( RestRequestContext rc, ClientHeaderParam p )
	{
		try {
			rc.getHeaders().put( p.name(), translateHeaders( rc, p ) );
		}
		catch( final EvalException e ) {
			if( p.required() ) {
				throw RBUtils.wrapException( e.getCause() );
			}
			else {
				rc.getHeaders().remove( p.name() );
			}
		}
	}

	private List<String> translateHeaders( RestRequestContext rc, ClientHeaderParam p )
	{
		return stream( p.value() )
			.map( StringUtils::trimToNull )
			.filter( Objects::nonNull )
			.map( v -> translateHeader( rc, p.name(), p.required(), v ) )
			.flatMap( Stream::of )
			.collect( toList() );
	}

	private String[] translateHeader( RestRequestContext rc, String name, boolean required, String value )
	{
		final String methodName = RBUtils.getExpression( value );

		if( methodName != null ) {
			try {
				return evalMethod( rc, name, methodName, required );
			}
			catch( final Throwable e ) {
				throw new EvalException( e );
			}
		}
		else {
			return new String[] { value };
		}
	}

	private String[] evalMethod( RestRequestContext rc, String headerName, String methodName, boolean required ) throws IllegalAccessException, InvocationTargetException
	{
		final RestMethodInfo mi = rc.getMethodInfo();
		final Method eval = RestBridgeListener.lookupMethod( mi.getServiceType(), methodName );
		Object result = null;

		if( Modifier.isStatic( eval.getModifiers() ) ) {
			result = eval.getParameterCount() == 0 ? eval.invoke( null ) : eval.invoke( null, headerName );
		}
		else {
			result = eval.getParameterCount() == 0 ? eval.invoke( rc.getService() ) : eval.invoke( rc.getService(), headerName );
		}

		if( result == null ) {
			return new String[0];
		}
		if( result instanceof String[] ) {
			return (String[]) result;
		}
		else {
			return new String[] { (String) result };
		}
	}
}
