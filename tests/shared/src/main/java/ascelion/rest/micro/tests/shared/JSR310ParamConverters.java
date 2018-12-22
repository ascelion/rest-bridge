
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

@Provider
public class JSR310ParamConverters implements ParamConverterProvider
{

	static public final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
		.appendLiteral( "DATE: " )
		.appendValue( ChronoField.YEAR, 4 )
		.appendValue( ChronoField.MONTH_OF_YEAR, 2 )
		.appendValue( ChronoField.DAY_OF_MONTH, 2 )
		.toFormatter();

	@Override
	public <T> ParamConverter<T> getConverter( Class<T> rawType, Type genericType, Annotation[] annotations )
	{
		if( rawType == LocalDate.class ) {
			return (ParamConverter<T>) new LocalDateParamCVT();
		}

		return null;
	}

}
