
package ascelion.rest.bridge.web;


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
