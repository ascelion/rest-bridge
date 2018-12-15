
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;

class ConvertersFactory
{

	static class DefaultPC<T> implements ParamConverter<T>
	{

		@Override
		public T fromString( String value )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString( T value )
		{
			return Objects.toString( value, null );
		}
	}

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
	private final Collection<ParamConverterProvider> providers;

	ConvertersFactory( Configuration cf )
	{
		Function<Class, Object> bld = (Function<Class, Object>) cf.getProperties().get( RestClient.INSTANTIATOR_PROPERTY );

		if( bld == null ) {
			bld = this::newInstance;
		}

		final Stream<ParamConverterProvider> si = cf
			.getInstances()
			.stream()
			.filter( ParamConverterProvider.class::isInstance )
			.map( ParamConverterProvider.class::cast );
		final Stream<ParamConverterProvider> sc = cf
			.getClasses()
			.stream()
			.filter( ParamConverterProvider.class::isAssignableFrom )
			.map( bld::apply )
			.map( ParamConverterProvider.class::cast );

		this.providers = Stream.concat( si, sc )
			.sorted( Util::byPriority )
			.collect( toList() );
		this.providers.add( new ParamConverterProvider()
		{

			@Override
			public <T> ParamConverter<T> getConverter( Class<T> rawType, Type genericType, Annotation[] annotations )
			{
				return new DefaultPC<>();
			}
		} );
	}

	Function<Object, String> getConverter( Class<?> type, Annotation[] annotations )
	{
		return this.converters.computeIfAbsent( new KEY( type, annotations ), this::findConverter );
	}

	private Function<Object, String> findConverter( KEY key )
	{
		final ParamConverter c = (ParamConverter) this.providers.stream()
			.map( p -> p.getConverter( key.type, key.type, key.annotations ) )
			.filter( Objects::nonNull )
			.findFirst()
			.get();

		return v -> {
			if( v == null ) {
				return null;
			}
			if( v instanceof String ) {
				return trimToNull( (String) v );
			}

			return trimToNull( c.toString( v ) );
		};
	}

	private Object newInstance( Class type )
	{
		try {
			return type.newInstance();
		}
		catch( InstantiationException | IllegalAccessException e ) {
			// TODO define exception
			throw new RuntimeException( e );
		}
	}
}
