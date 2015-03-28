
package ascelion.rest.bridge.client;

public interface RestCallback<T>
{

	T apply( T t );
}
