
package ascelion.rest.bridge.tests.app;

import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.api.Validated;

public class ValidatedImpl
implements Validated
{

	@Override
	public BeanData bean( BeanData value )
	{
		return value;
	}

	@Override
	public BeanData beanNotNull( BeanData value )
	{
		return value;
	}

	@Override
	public BeanData beanValid( BeanData value )
	{
		return value;
	}

	@Override
	public BeanData beanValidNotNull( BeanData value )
	{
		return value;
	}

	@Override
	public String notNullFormParam( String value )
	{
		return value;
	}

	@Override
	public String notNullHeaderParam( String value )
	{
		return value;
	}

	@Override
	public String notNullQueryParam( String value )
	{
		return value;
	}

	@Override
	public BeanData notNullWithBean( String value, BeanData bean )
	{
		bean.setNotNull( value );

		return bean;
	}

}
