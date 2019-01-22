
package ascelion.rest.bridge.etc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Provider
@Priority( Integer.MAX_VALUE )
public final class JSR310ParamConverters implements ParamConverterProvider
{

	@Priority( Integer.MAX_VALUE )
	static class LocalDateCV implements ParamConverter<LocalDate>
	{

		@Override
		public LocalDate fromString( String value )
		{
			value = trimToNull( value );

			return value != null ? LocalDate.parse( value, DateTimeFormatter.ISO_DATE ) : null;
		}

		@Override
		public String toString( LocalDate value )
		{
			return value != null ? value.format( DateTimeFormatter.ISO_DATE ) : null;
		}
	};

	@Priority( Integer.MAX_VALUE )
	static class LocalDateTimeCV implements ParamConverter<LocalDateTime>
	{

		@Override
		public LocalDateTime fromString( String value )
		{
			value = trimToNull( value );

			return value != null ? LocalDateTime.parse( value, DateTimeFormatter.ISO_DATE_TIME ) : null;
		}

		@Override
		public String toString( LocalDateTime value )
		{
			return value != null ? value.format( DateTimeFormatter.ISO_DATE_TIME ) : null;
		}
	};

	@Priority( Integer.MAX_VALUE )
	static class LocalTimeCV implements ParamConverter<LocalTime>
	{

		@Override
		public LocalTime fromString( String value )
		{
			value = trimToNull( value );

			return value != null ? LocalTime.parse( value, DateTimeFormatter.ISO_TIME ) : null;
		}

		@Override
		public String toString( LocalTime value )
		{
			return value != null ? value.format( DateTimeFormatter.ISO_TIME ) : null;
		}
	};

	private static final Map<Class<?>, ParamConverter<?>> CONVERTERS = new IdentityHashMap<>();

	static {
		CONVERTERS.put( LocalDate.class, new LocalDateCV() );
		CONVERTERS.put( LocalTime.class, new LocalTimeCV() );
		CONVERTERS.put( LocalDateTime.class, new LocalDateTimeCV() );
	}

	@Override
	public <T> ParamConverter<T> getConverter( Class<T> rawType, Type genericType, Annotation[] annotations )
	{
		return (ParamConverter<T>) CONVERTERS.get( rawType );
	}

}
