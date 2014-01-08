
package ascelion.rest.bridge.web;

public class ValidatedImpl
implements Validated
{

	@Override
	public BeanValidData bean( BeanValidData value )
	{
		return value;
	}

	@Override
	public BeanValidData beanNotNull( BeanValidData value )
	{
		return value;
	}

	@Override
	public BeanValidData beanValid( BeanValidData value )
	{
		return value;
	}

	@Override
	public BeanValidData beanValidNotNull( BeanValidData value )
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
	public BeanValidData notNullWithBean( String value, BeanValidData bean )
	{
		bean.setNotNull( value );

		return bean;
	}

}
