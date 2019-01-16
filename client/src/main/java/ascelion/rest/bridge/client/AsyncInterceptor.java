
package ascelion.rest.bridge.client;

public interface AsyncInterceptor<T>
{

	T prepare();

	default void before( T t )
	{
	}

	default void after( T t )
	{
	}
}
