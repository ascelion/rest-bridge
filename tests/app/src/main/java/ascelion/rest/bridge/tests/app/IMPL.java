
package ascelion.rest.bridge.tests.app;

import ascelion.rest.bridge.tests.api.API;

public abstract class IMPL<T>
implements API<T>
{

	@Override
	public T create( T t )
	{
		return t;
	}

	@Override
	public void delete( T t )
	{
	}

	@Override
	public T update( T t )
	{
		return t;
	}

}
