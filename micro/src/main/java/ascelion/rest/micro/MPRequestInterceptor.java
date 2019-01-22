
package ascelion.rest.micro;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestRequestContext;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.apache.commons.lang3.reflect.MethodUtils.getMatchingMethod;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;

public class MPRequestInterceptor implements Function<RestRequestContext, RestRequestContext>
{

	private final ThreadLocalValue<HttpHeaders> tlHeaders = ThreadLocalProxy.create( HttpHeaders.class );

	@Override
	public RestRequestContext apply( RestRequestContext rc )
	{
		final Method met = rc.getJavaMethod();

		Stream.of( met.getAnnotationsByType( ClientHeaderParam.class ) )
			.forEach( p -> addHeader( rc, p ) );
		;

		getAllInterfaces( rc.getInterface().getClass() ).stream()
			.filter( t -> t.isAssignableFrom( rc.getInterfaceType() ) )
			.flatMap( t -> Stream.of( t.getAnnotationsByType( ClientHeaderParam.class ) ) )
			.forEach( p -> addHeader( rc, p ) );
		;

		getAllInterfaces( rc.getInterface().getClass() ).stream()
			.filter( t -> t.isAssignableFrom( rc.getInterfaceType() ) )
			.flatMap( t -> Stream.of( t.getAnnotationsByType( RegisterClientHeaders.class ) ) );

		ofNullable( rc.getInterfaceType().getAnnotation( RegisterClientHeaders.class ) )
			.ifPresent( a -> headersFactory( rc, a ) );

		return rc;
	}

	private void headersFactory( RestRequestContext rc, RegisterClientHeaders a )
	{
		final MultivaluedMap<String, String> incHeaders = this.tlHeaders.get().getRequestHeaders();
		final MultivaluedMap<String, String> headers = RBUtils.newInstance( a.value() )
			.update( incHeaders, rc.getHeaders() );

		rc.getHeaders().putAll( headers );
	}

	private void addHeader( RestRequestContext rc, ClientHeaderParam p )
	{
		rc.getHeaders().addAll( p.name(), translateHeaders( rc, p ) );
	}

	private String[] translateHeaders( RestRequestContext rc, ClientHeaderParam p )
	{
		return Stream.of( p.value() )
			.map( StringUtils::trimToNull )
			.filter( Objects::nonNull )
			.map( v -> translateHeader( rc, p.name(), p.required(), v ) )
			.flatMap( Stream::of )
			.toArray( String[]::new );
	}

	private String[] translateHeader( RestRequestContext rc, String name, boolean required, String value )
	{
		if( value != null && value.length() > 2 && value.startsWith( "{" ) && value.endsWith( "}" ) ) {
			final Method eval = lookupMethod( rc, value.substring( 1, value.length() - 1 ) );

			if( eval == null ) {
				return new String[0];
			}

			Object result = null;

			try {
				if( Modifier.isStatic( eval.getModifiers() ) ) {
					result = eval.getParameterCount() == 0 ? eval.invoke( null ) : eval.invoke( null, name );
				}
				else {
					result = eval.getParameterCount() == 0 ? eval.invoke( rc.getInterface() ) : eval.invoke( rc.getInterface(), name );
				}
			}
			catch( final Throwable e ) {
				if( required ) {
					wrapException( e );
				}

				return new String[0];
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
		else {
			return new String[] { value };
		}
	}

	private Method lookupMethod( RestRequestContext rc, String name )
	{
		final int dot = name.lastIndexOf( '.' );

		if( dot > 0 ) {
			final Class<?> cls = RBUtils.safeLoadClass( name.substring( 0, dot - 1 ) );

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
