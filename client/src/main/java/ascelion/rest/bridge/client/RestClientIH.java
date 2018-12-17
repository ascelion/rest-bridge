
package ascelion.rest.bridge.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.client.WebTarget;

import static java.util.Arrays.asList;

final class RestClientIH
implements InvocationHandler
{

	static private final Collection<Method> O_METHODS = asList( Object.class.getMethods() );

	private final ConvertersFactory cvsf;
	private final Supplier<WebTarget> target;
	private final Class<?> cls;

	private final Map<Method, RestMethod> methods = new HashMap<>();

	RestClientIH( Class<?> cls, ConvertersFactory cvsf, Supplier<WebTarget> target )
	{
		this.target = target;
		this.cls = cls;
		this.cvsf = cvsf;

		initMethods();

	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] arguments ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, arguments );
		}

		final RestMethod met = this.methods.get( method );

		if( met == null ) {
			// TODO
			throw new UnsupportedOperationException( "Could not handle method " + method );
		}

		return met.request( proxy, arguments ).run();
	}

	@Override
	public String toString()
	{
		return String.format( "%s -> %s", this.cls.getName(), this.target );
	}

	private void initMethods()
	{
		for( final Method m : this.cls.getMethods() ) {
			addMethod( m );
		}
	}

	private void addMethod( Method m )
	{
		this.methods.put( m, new RestMethod( this.cvsf, this.cls, m, this.target ) );
	}

	<X> X newProxy()
	{
		return (X) Proxy.newProxyInstance( this.cls.getClassLoader(), new Class[] { this.cls }, this );
	}
}
