
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import ascelion.utils.etc.SimpleTypeBuilder;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

final class ConvertersFactoryImpl implements ConvertersFactory
{

	private static class NullablePC<T> implements LazyParamConverter<T>
	{

		private final Supplier<ParamConverter<T>> sup;

		NullablePC( Supplier<ParamConverter<T>> sup )
		{
			this.sup = sup;
		}

		@Override
		public T fromString( String value )
		{
			value = trimToNull( value );

			return value != null ? this.sup.get().fromString( value ) : null;
		}

		@Override
		public String toString( T value )
		{
			return value != null ? trimToNull( this.sup.get().toString( value ) ) : null;
		}

		@Override
		public boolean isLazy()
		{
			return RBUtils.findAnnotation( ParamConverter.Lazy.class, this.sup.get().getClass() ).map( a -> true ).orElse( false );
		}
	}

	@ParamConverter.Lazy
	@RequiredArgsConstructor
	static private class DefaultPC<T> implements ParamConverter<T>
	{

		static private final SimpleTypeBuilder STB = new SimpleTypeBuilder();

		private final Class<T> type;

		@Override
		public T fromString( String value )
		{
			return STB.createFromPlainText( this.type, value );
		}

		@Override
		public String toString( T value )
		{
			return Objects.toString( value, null );
		}
	};

	static private final ParamConverterProvider DEFAULT_PCP = new ParamConverterProvider()
	{

		@Override
		public <T> ParamConverter<T> getConverter( Class<T> type, Type gt, Annotation[] annotations )
		{
			return new DefaultPC<>( type );
		}
	};

	@EqualsAndHashCode
	@ToString
	static final class KEY
	{

		private final Class<?> type;
		private final Annotation[] annotations;

		KEY( Class<?> type, Annotation[] annotations )
		{
			this.type = type;
			this.annotations = Stream.of( annotations )
				.sorted( ( a1, a2 ) -> a1.annotationType().getName().compareTo( a2.annotationType().getName() ) )
				.toArray( Annotation[]::new );
		}
	}

	private final Map<KEY, ParamConverter<?>> converters = new ConcurrentHashMap<>();
	private final Configuration cf;

	ConvertersFactoryImpl( Configurable<?> cf )
	{
		this.cf = cf.getConfiguration();

		if( !this.cf.isRegistered( DEFAULT_PCP ) ) {
			cf.register( DEFAULT_PCP, Integer.MAX_VALUE );
		}
	}

	@Override
	public <T> LazyParamConverter<T> getConverter( Class<T> type, Annotation[] annotations )
	{
		return new NullablePC<>( () -> lookupConverter( type, annotations ) );
	}

	private <T> ParamConverter<T> lookupConverter( Class<?> type, Annotation[] annotations )
	{
		return (ParamConverter<T>) this.converters.computeIfAbsent( new KEY( type, annotations ), k -> findConverter( k ) );
	}

	private <T> ParamConverter<T> findConverter( KEY key )
	{
		final ParamConverter<?> c = ConfigurationEx.providers( this.cf, ParamConverterProvider.class ).stream()
			.map( p -> p.getInstance().getConverter( key.type, key.type, key.annotations ) )
			.filter( Objects::nonNull )
			.findFirst()
			.orElseGet( () -> (ParamConverter) DEFAULT_PCP.getConverter( key.type, key.type, key.annotations ) );

		return (ParamConverter<T>) c;
	}
}
