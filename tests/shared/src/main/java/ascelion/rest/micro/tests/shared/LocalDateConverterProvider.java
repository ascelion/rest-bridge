
package ascelion.rest.micro.tests.shared;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Provider
public class LocalDateConverterProvider implements ParamConverterProvider
{

	public static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
		.appendLiteral( "DATE: " )
		.appendValue( ChronoField.YEAR, 4 )
		.appendValue( ChronoField.MONTH_OF_YEAR, 2 )
		.appendValue( ChronoField.DAY_OF_MONTH, 2 )
		.toFormatter();

	static class LocalDateParamCVT implements ParamConverter<LocalDate>
	{

		@Override
		public LocalDate fromString( String value )
		{
			value = trimToNull( value );

			return value != null ? LocalDate.parse( value, DATE_FORMAT ) : null;
		}

		@Override
		public String toString( LocalDate value )
		{
			return value != null ? value.format( DATE_FORMAT ) : null;
		}
	}

	@Override
	public <T> ParamConverter<T> getConverter( Class<T> rawType, Type genericType, Annotation[] annotations )
	{
		if( rawType == LocalDate.class ) {
			return (ParamConverter<T>) new LocalDateParamCVT();
		}

		return null;
	}

}
