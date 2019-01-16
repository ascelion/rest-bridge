
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;

class ConvertersFactory
{

	static private final ParamConverter<?> DEFAULT_PC = new ParamConverter<Object>()
	{

		@Override
		public Object fromString( String value )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString( Object value )
		{
			return Objects.toString( value, null );
		}
	};

	static private final ParamConverterProvider DEFAULT_PCP = new ParamConverterProvider()
	{

		@Override
		public <T> ParamConverter<T> getConverter( Class<T> rawType, Type genericType, Annotation[] annotations )
		{
			return (ParamConverter<T>) DEFAULT_PC;
		}
	};

	@EqualsAndHashCode
	@ToString
	static private final class KEY
	{

		private final Class type;
		private final Annotation[] annotations;

		KEY( Class type, Annotation[] annotations )
		{
			this.type = type;
			this.annotations = Stream.of( annotations )
				.sorted( ( a1, a2 ) -> a1.annotationType().getName().compareTo( a2.annotationType().getName() ) )
				.toArray( Annotation[]::new );
		}
	}

	private final Map<KEY, Function<Object, String>> converters = new ConcurrentHashMap<>();
	private final Configuration cf;

	ConvertersFactory( Client client )
	{
		this.cf = client.getConfiguration();

		if( !this.cf.isRegistered( DEFAULT_PCP ) ) {
			client.register( DEFAULT_PCP, Integer.MAX_VALUE );
		}
	}

	<T> Function<T, String> getConverter( Class<T> type, Annotation[] annotations )
	{
		return t -> this.converters.computeIfAbsent( new KEY( type, annotations ), this::findConverter ).apply( t );
	}

	private <T> Function<T, String> findConverter( KEY key )
	{
		final Collection<ParamConverterProvider> providers = RBUtils.providers( this.cf, ParamConverterProvider.class );
		final ParamConverter<T> c = (ParamConverter<T>) providers.stream()
			.map( p -> p.getConverter( key.type, key.type, key.annotations ) )
			.filter( Objects::nonNull )
			.findFirst()
			.get();

		return v -> {
			if( v == null ) {
				return null;
			}

			return trimToNull( c.toString( v ) );
		};
	}
}
