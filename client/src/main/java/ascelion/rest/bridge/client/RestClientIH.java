
package ascelion.rest.bridge.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import javassist.util.proxy.ProxyFactory;

final class RestClientIH
{

	static private final Collection<Method> O_METHODS = asList( Object.class.getMethods() );
	static private final Constructor<MethodHandles.Lookup> LOOKUP;

	static {
		try {
			LOOKUP = MethodHandles.Lookup.class.getDeclaredConstructor( Class.class, int.class );

			LOOKUP.setAccessible( true );
		}
		catch( NoSuchMethodException | SecurityException e ) {
			throw new ExceptionInInitializerError( e );
		}
	}

	private final RestClientData rcd;
	private final Map<Method, RestMethod2> methods = new HashMap<>();

	RestClientIH( RestClientData rcd )
	{
		this.rcd = rcd;

		initMethods();
	}

	@Override
	public String toString()
	{
		return String.format( "%s -> %s", this.rcd.type.getName(), this.rcd.tsup.get() );
	}

	<X> X newProxy()
	{
		if( this.rcd.type.isInterface() ) {
			return (X) Proxy.newProxyInstance( this.rcd.type.getClassLoader(), new Class[] { this.rcd.type }, this::invoke );
		}
		else {
			final ProxyFactory pf = new ProxyFactory();

			try {
				pf.setSuperclass( this.rcd.type );

				return (X) pf.create( new Class[0], new Object[0], ( self, thisMethod, proceed, args ) -> invoke( self, proceed, args ) );
			}
			catch( final RuntimeException e ) {
				throw e;
			}
			catch( final InvocationTargetException e ) {
				throw new RuntimeException( format( "Cannot proxy class %s", this.rcd.type.getName() ), e.getCause() );
			}
			catch( final NoSuchMethodException | InstantiationException | IllegalAccessException e ) {
				throw new RuntimeException( format( "Cannot proxy class %s", this.rcd.type.getName() ), e );
			}
		}

	}

	private void initMethods()
	{
		for( final Method m : this.rcd.type.getMethods() ) {
			if( O_METHODS.contains( m ) ) {
				continue;
			}

			if( !m.isDefault() ) {
				addMethod( m );
			}
		}
	}

	private void addMethod( Method m )
	{
		final RestMethod2 x = new RestMethod2( this.rcd, m );

		this.methods.put( m, x );
	}

	private Object invoke( Object proxy, Method method, Object[] arguments ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, arguments );
		}

		final RestMethod2 met = this.methods.get( method );

		if( met != null ) {
			return met.request( proxy, arguments );
		}
		else if( method.isDefault() ) {
			final Class<?> cls = method.getDeclaringClass();
			final MethodHandle han = LOOKUP.newInstance( cls, -1 )
				.unreflectSpecial( method, cls )
				.bindTo( proxy );

			return han.invokeWithArguments( arguments );
		}

		throw new RestClientMethodException( "Could not handle method ", method );
	}

}
