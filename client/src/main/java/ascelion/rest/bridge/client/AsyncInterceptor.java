
package ascelion.rest.bridge.client;

public interface AsyncInterceptor<T>
{

	AsyncInterceptor<Object> NONE = () -> null;

	T prepare();

	default void before( T t )
	{
	}

	default void after( T t )
	{
	}
}
