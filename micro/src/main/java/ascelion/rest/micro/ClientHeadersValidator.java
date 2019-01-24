
package ascelion.rest.micro;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.ws.rs.HeaderParam;

import ascelion.rest.bridge.client.RBUtils;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.apache.commons.lang3.reflect.MethodUtils.getMatchingMethod;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

public final class ClientHeadersValidator implements RestClientListener
{

	static String getMethodName( String value )
	{
		return value.length() > 2 && value.startsWith( "{" ) && value.endsWith( "}" )
			? value.substring( 1, value.length() - 1 )
			: null;
	}

	static Method lookupMethod( Class<?> type, String name )
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
			return ofNullable( getMatchingMethod( type, name ) )
				.orElse( getMatchingMethod( type, name, String.class ) );
		}
	}

	@Override
	public void onNewClient( Class<?> type, RestClientBuilder bld )
	{
		checkUniqueHeaderName( type, type );
		getAllInterfaces( type )
			.forEach( t -> checkUniqueHeaderName( t, t ) );

		stream( type.getMethods() ).forEach( m -> {
			checkUniqueHeaderName( type, m );
			checkUniqueHeaderParam( m );
		} );
	}

	private void checkUniqueHeaderName( Class<?> type, AnnotatedElement element )
	{
		final Function<ClientHeaderParam, String> kfun = ClientHeaderParam::name;
		final Function<ClientHeaderParam, Integer> vfun = a -> 1;
		final BinaryOperator<Integer> mfun = ( a, b ) -> {
			return a + b;
		};

		final String dups = stream( element.getAnnotationsByType( ClientHeaderParam.class ) )
			.collect( toMap( kfun, vfun, mfun ) )
			.entrySet()
			.stream()
			.filter( e -> e.getValue() > 1 )
			.map( e -> e.getKey() )
			.collect( joining( ", " ) );

		if( dups.length() > 0 ) {
			final String tx = format( "Multiple headers specified on %s: %s", element, dups );

			RestBridgeConfiguration.LOG.severe( tx );

			throw new RestClientDefinitionException( tx );
		}

		final String invalid = stream( element.getAnnotationsByType( ClientHeaderParam.class ) )
			.map( a -> new ImmutablePair<>( a.name(), invalidMethod( type, a.value() ) ) )
			.filter( p -> p.right.length() > 0 )
			.map( p -> format( "%s(%s)", p.left, p.right ) )
			.collect( joining( ", " ) );

		if( invalid.length() > 0 ) {
			final String tx = format( "Invalid methods specified on %s: %s", element, invalid );

			RestBridgeConfiguration.LOG.severe( tx );

			throw new RestClientDefinitionException( tx );
		}
	}

	private String invalidMethod( Class<?> type, String[] value )
	{
		final Set<String> set = stream( value ).map( v -> getMethodName( v ) )
			.collect( toSet() );

		if( set.contains( null ) ) {
			return set.size() > 1 ? "multiple values" : "";
		}

		final String methodName = set.iterator().next();
		final Method method = lookupMethod( type, methodName );

		if( method == null ) {
			return "invalid method: " + methodName;
		}

		return "";
	}

	private void checkUniqueHeaderParam( Method m )
	{
		final Function<HeaderParam, String> kfun = HeaderParam::value;
		final Function<HeaderParam, Integer> vfun = a -> 1;
		final BinaryOperator<Integer> mfun = ( a, b ) -> {
			return a + b;
		};

		final String dups = stream( m.getParameters() )
			.filter( p -> p.isAnnotationPresent( HeaderParam.class ) )
			.map( p -> p.getAnnotation( HeaderParam.class ) )
			.collect( toMap( kfun, vfun, mfun ) )
			.entrySet()
			.stream()
			.filter( e -> e.getValue() > 1 )
			.map( e -> e.getKey() )
			.collect( joining( ", " ) );
		;

		if( dups.length() > 0 ) {
			final String tx = format( "Multiple headers specified on %s: %s", m, dups );

			RestBridgeConfiguration.LOG.severe( tx );

			throw new RestClientDefinitionException( tx );
		}
	}

}
