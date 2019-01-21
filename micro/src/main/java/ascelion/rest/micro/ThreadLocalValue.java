
package ascelion.rest.micro;

public interface ThreadLocalValue<T>
{

	void set( T t );

	T get();

	boolean isPresent();
}
