
package ascelion.rest.micro;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.commons.lang3.reflect.MethodUtils.getMatchingMethod;

import com.google.common.collect.MapMaker;

final class ThreadLocalProxy<T> implements InvocationHandler
{

	static <X> ThreadLocalValue<X> create( Class<X> type )
	{
		final Class[] itfs = new Class[] { type, ThreadLocalValue.class };
		final ThreadLocalProxy<X> ih = new ThreadLocalProxy<>( type );

		return (ThreadLocalValue<X>) newProxyInstance( currentThread().getContextClassLoader(), itfs, ih );
	}

	static private final Map<Class<?>, ThreadLocal<?>> TLS = new MapMaker().weakKeys().makeMap();

	private final ThreadLocal<T> tl;
	private final Map<Method, InvocationHandler> handlers = new HashMap<>();

	private ThreadLocalProxy( Class<T> type )
	{
		this.tl = (ThreadLocal<T>) TLS.computeIfAbsent( type, t -> new ThreadLocal<>() );

		this.handlers.put( getMatchingMethod( ThreadLocalValue.class, "set", type ), ( o, m, a ) -> tlv_set( o, a[0] ) );
		this.handlers.put( getMatchingMethod( ThreadLocalValue.class, "get" ), ( o, m, a ) -> o );
		this.handlers.put( getMatchingMethod( ThreadLocalValue.class, "isPresent" ), ( o, m, a ) -> this.tl.get() != null );

		Stream.of( type.getMethods() )
			.forEach( m -> this.handlers.put( m, this::invokeThreadLocal ) );
		;

	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
	{
		return this.handlers.getOrDefault( method, this::invokeThis )
			.invoke( proxy, method, args );
	}

	private Object tlv_set( Object proxy, Object a )
	{
		if( a == null ) {
			this.tl.remove();
		}
		else {
			this.tl.set( (T) a );
		}

		return null;
	}

	private Object invokeThis( Object proxy, Method method, Object[] args ) throws Throwable
	{
		return method.invoke( this, args );
	}

	private Object invokeThreadLocal( Object proxy, Method method, Object[] args ) throws Throwable
	{
		final T t = this.tl.get();

		if( t == null ) {
			// TODO
			throw new IllegalStateException( "NOT SET!!!" );
		}

		return method.invoke( t, args );
	}
}
