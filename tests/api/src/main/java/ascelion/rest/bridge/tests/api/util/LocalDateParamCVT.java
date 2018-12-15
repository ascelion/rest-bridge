
package ascelion.rest.bridge.tests.api.util;

import java.time.LocalDate;

import javax.ws.rs.ext.ParamConverter;

import static ascelion.rest.bridge.tests.api.util.JSR310ParamConverters.DATE_FORMAT;
import static org.apache.commons.lang3.StringUtils.trimToNull;

final class LocalDateParamCVT implements ParamConverter<LocalDate>
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
