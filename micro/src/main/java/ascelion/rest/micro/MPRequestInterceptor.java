
package ascelion.rest.micro;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RequestInterceptor;
import ascelion.rest.bridge.client.RestRequestContext;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.apache.commons.lang3.reflect.MethodUtils.getMatchingMethod;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

public class MPRequestInterceptor implements RequestInterceptor
{

	static class EvalException extends RuntimeException
	{

		EvalException( Throwable cause )
		{
			super( cause );
		}
	}

	private final ThreadLocalValue<HttpHeaders> headers = ThreadLocalProxy.create( HttpHeaders.class );

	@Override
	public RestRequestContext before( RestRequestContext rc )
	{
		if( this.headers.isAbsent() ) {
			this.headers.set( new HttpHeadersImpl( rc ) );
		}

		final Method met = rc.getJavaMethod();

		final Map<String, Runnable> actions = new TreeMap<>();

		getAllInterfaces( rc.getInterface().getClass() ).stream()
			.filter( t -> t.isAssignableFrom( rc.getInterfaceType() ) )
			.flatMap( t -> stream( t.getAnnotationsByType( ClientHeaderParam.class ) ) )
			.forEach( a -> actions.put( a.name(), () -> setHeader( rc, a ) ) );
		;

		stream( met.getAnnotationsByType( ClientHeaderParam.class ) )
			.forEach( a -> actions.put( a.name(), () -> setHeader( rc, a ) ) );
		;

		// repeat HeaderParamAction, definitely need a refactoring
		for( int x = 0; x < met.getParameterCount(); x++ ) {
			final Parameter p = met.getParameters()[x];
			final HeaderParam a = p.getAnnotation( HeaderParam.class );

			if( a != null ) {
				final Object v = rc.getArgumentAt( x );

				actions.put( a.value(), () -> {
					final Function<Object, String> c = (Function) rc.getConverter( p.getType(), p.getAnnotations() );

					rc.getHeaders().putSingle( a.value(), c.apply( v ) );
				} );
			}
		}

		actions.values().forEach( Runnable::run );

		ofNullable( rc.getInterfaceType().getAnnotation( RegisterClientHeaders.class ) )
			.ifPresent( a -> headersFactory( rc, a ) );

		try {
			org.jboss.resteasy.spi.ResteasyProviderFactory.pushContext( HttpHeaders.class, this.headers.get() );
		}
		catch( final NoClassDefFoundError e ) {
			;
		}

		return rc;
	}

	@Override
	public void after( RestRequestContext rc )
	{
		this.headers.set( null );

		try {
			org.jboss.resteasy.spi.ResteasyProviderFactory.popContextData( HttpHeaders.class );
		}
		catch( final NoClassDefFoundError e ) {
			;
		}
	}

	private void headersFactory( RestRequestContext rc, RegisterClientHeaders a )
	{
		final MultivaluedMap<String, String> incHeaders = this.headers.get().getRequestHeaders();
		final ClientHeadersFactory factory = RBUtils.newInstance( a.value() );

		new TypeDesc<>( factory ).inject( factory );

		final MultivaluedMap<String, String> headers = factory
			.update( incHeaders, rc.getHeaders() );

		rc.getHeaders().putAll( headers );
	}

	private void setHeader( RestRequestContext rc, ClientHeaderParam p )
	{
		try {
			rc.getHeaders().put( p.name(), translateHeaders( rc, p ) );
		}
		catch( final EvalException e ) {
			if( p.required() ) {
				wrapException( e.getCause() );
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
		if( value != null && value.length() > 2 && value.startsWith( "{" ) && value.endsWith( "}" ) ) {
			try {
				return evalMethod( rc, name, value, required );
			}
			catch( final Throwable e ) {
				throw new EvalException( e );
			}
		}
		else {
			return new String[] { value };
		}
	}

	private String[] evalMethod( RestRequestContext rc, String name, String value, boolean required ) throws IllegalAccessException, InvocationTargetException
	{
		final Method eval = lookupMethod( rc, value.substring( 1, value.length() - 1 ) );

		if( eval == null ) {
			return new String[0];
		}

		Object result = null;

		if( Modifier.isStatic( eval.getModifiers() ) ) {
			result = eval.getParameterCount() == 0 ? eval.invoke( null ) : eval.invoke( null, name );
		}
		else {
			result = eval.getParameterCount() == 0 ? eval.invoke( rc.getInterface() ) : eval.invoke( rc.getInterface(), name );
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

	private Method lookupMethod( RestRequestContext rc, String name )
	{
		final int dot = name.lastIndexOf( '.' );

		if( dot > 0 ) {
			final Class<?> cls = RBUtils.safeLoadClass( name.substring( 0, dot ) );

			if( cls == null ) {
				return null;
			}

			name = name.substring( dot + 1 );

			return ofNullable( getMatchingMethod( cls, name ) )
				.orElse( getMatchingMethod( cls, name, String.class ) );
		}
		else {
			return ofNullable( getMatchingMethod( rc.getInterfaceType(), name ) )
				.orElse( getMatchingMethod( rc.getInterfaceType(), name, String.class ) );
		}
	}

	private void wrapException( Throwable e )
	{
		if( e instanceof InvocationTargetException ) {
			wrapException( e.getCause() );
		}
		if( e instanceof Error ) {
			throw(Error) e;
		}
		if( e instanceof RuntimeException ) {
			throw(RuntimeException) e;
		}

		throw new RuntimeException( e );
	}

}
