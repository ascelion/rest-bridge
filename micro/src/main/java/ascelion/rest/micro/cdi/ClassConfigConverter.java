
package ascelion.rest.micro.cdi;

import ascelion.rest.bridge.client.RBUtils;

import org.eclipse.microprofile.config.spi.Converter;

public class ClassConfigConverter implements Converter<Class<?>>
{

	@Override
	public Class<?> convert( String value )
	{
		return RBUtils.rtLoadClass( value );
	}

}
