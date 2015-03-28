
package ascelion.rest.bridge.client;

class RestCallbackWrapper<T>
implements RestCallback<T>
{

	final RestCallback<T> callback;

	RestCallbackWrapper( RestCallback<T> callback )
	{
		this.callback = callback;
	}

	@Override
	public T apply( T t )
	{
		return this.callback != null ? this.callback.apply( t ) : t;
	}
}
