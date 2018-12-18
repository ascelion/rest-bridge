
package ascelion.rest.bridge.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.client.WebTarget;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import javassist.util.proxy.ProxyFactory;

final class RestClientIH
{

	static private final Collection<Method> O_METHODS = asList( Object.class.getMethods() );

	private final Class<?> type;
	private final ConvertersFactory cvsf;
	private final Supplier<WebTarget> target;

	private final Map<Method, RestMethod> methods = new HashMap<>();

	RestClientIH( Class<?> type, ConvertersFactory cvsf, Supplier<WebTarget> target )
	{
		this.type = type;
		this.cvsf = cvsf;
		this.target = target;

		initMethods();
	}

	@Override
	public String toString()
	{
		return String.format( "%s -> %s", this.type.getName(), this.target );
	}

	<X> X newProxy()
	{
		if( this.type.isInterface() ) {
			return (X) Proxy.newProxyInstance( this.type.getClassLoader(), new Class[] { this.type }, this::invoke );
		}
		else {
			final ProxyFactory pf = new ProxyFactory();

			try {
				pf.setSuperclass( this.type );

				return (X) pf.create( new Class[0], new Object[0], ( self, thisMethod, proceed, args ) -> invoke( self, proceed, args ) );
			}
			catch( final RuntimeException e ) {
				throw e;
			}
			catch( final InvocationTargetException e ) {
				throw new RuntimeException( format( "Cannot proxy class %s", this.type.getName() ), e.getCause() );
			}
			catch( final NoSuchMethodException | InstantiationException | IllegalAccessException e ) {
				throw new RuntimeException( format( "Cannot proxy class %s", this.type.getName() ), e );
			}
		}

	}

	private void initMethods()
	{
		for( final Method m : this.type.getMethods() ) {
			if( O_METHODS.contains( m ) ) {
				continue;
			}

			addMethod( m );
		}
	}

	private void addMethod( Method m )
	{
		this.methods.put( m, new RestMethod( this.cvsf, this.type, m, this.target ) );
	}

	private Object invoke( Object proxy, Method method, Object[] arguments ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, arguments );
		}

		final RestMethod met = this.methods.get( method );

		if( met == null ) {
			// TODO
			throw new UnsupportedOperationException( "Could not handle method " + method );
		}

		return met.request( proxy, arguments ).call();
	}

}
