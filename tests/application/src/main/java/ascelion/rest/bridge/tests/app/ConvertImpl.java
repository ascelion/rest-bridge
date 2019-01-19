
package ascelion.rest.bridge.tests.app;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import ascelion.rest.bridge.tests.api.Convert;

import static ascelion.rest.micro.tests.shared.LocalDateConverterProvider.DATE_FORMAT;

@ApplicationScoped
public class ConvertImpl implements Convert
{

	@Override
	public String format( LocalDate date )
	{
		return date.format( DATE_FORMAT );
	}

	@Override
	public String formatPost( LocalDate date )
	{
		return date.format( DATE_FORMAT );
	}

}
