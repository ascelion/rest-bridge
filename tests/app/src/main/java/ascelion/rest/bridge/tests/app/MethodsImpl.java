
package ascelion.rest.bridge.tests.app;

import ascelion.rest.bridge.tests.api.Methods;

public class MethodsImpl
implements Methods
{

	@Override
	public void delete()
	{
	}

	@Override
	public String get()
	{
		return "GET";
	}

	@Override
	public String head()
	{
		return "HEAD";
	}

	@Override
	public String options()
	{
		return "OPTIONS";
	}

	@Override
	public void post()
	{
	}

	@Override
	public void put( String value )
	{
	}

}
