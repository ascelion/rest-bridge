
package ascelion.rest.bridge.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ascelion.utils.chain.AroundInterceptor;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import javassist.util.proxy.ProxyFactory;

final class RestService
{

	static private final Logger L = Logger.getLogger( "ascelion.rest.bridge.CONFIG" );
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

	private final RestClientInternals rci;
	private final RestServiceInfo rsi;
	private final Map<Method, RestMethod> methods = new HashMap<>();

	RestService( RestServiceInfo rsi, RestClientInternals rci )
	{
		this.rci = rci;
		this.rsi = rsi;

		initMethods();

		if( L.isLoggable( Level.CONFIG ) ) {
			try( Formatter fmt = new Formatter() ) {

				fmt.format( "Created %s\n", this );

				this.methods.values().forEach( m -> {
					fmt.format( "  %s\n", m );

					final Collection<AroundInterceptor<RestRequestContext>> ais = m.chain.getAll();

					if( ais.size() > 0 ) {
						fmt.format( "    %s: %s\n", m.chain, ais.stream().map( AroundInterceptor::about ).collect( joining( ", " ) ) );
					}
				} );

				L.config( fmt.toString() );
			}
		}
	}

	@Override
	public String toString()
	{
		return format( "%s -> %s", this.rsi.getServiceType().getName(), this.rsi.getTarget().get().getUriBuilder().toTemplate() );
	}

	<X> X newProxy()
	{
		if( this.rsi.getServiceType().isInterface() ) {
			return (X) Proxy.newProxyInstance( this.rsi.getServiceType().getClassLoader(), new Class[] { this.rsi.getServiceType() }, this::invoke );
		}
		else {
			final ProxyFactory pf = new ProxyFactory();

			try {
				pf.setSuperclass( this.rsi.getServiceType() );

				return (X) pf.create( new Class[0], new Object[0], ( self, thisMethod, proceed, args ) -> invoke( self, proceed, args ) );
			}
			catch( final RuntimeException e ) {
				throw e;
			}
			catch( final InvocationTargetException e ) {
				throw new RuntimeException( format( "Cannot proxy class %s", this.rsi.getServiceType().getName() ), e.getCause() );
			}
			catch( final NoSuchMethodException | InstantiationException | IllegalAccessException e ) {
				throw new RuntimeException( format( "Cannot proxy class %s", this.rsi.getServiceType().getName() ), e );
			}
		}

	}

	private void initMethods()
	{
		for( final Method m : this.rsi.getServiceType().getMethods() ) {
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
		final RestMethodInfo rmi = new RestMethodInfo( this.rsi, m );
		final RestMethod x = new RestMethod( rmi, this.rci );

		this.methods.put( m, x );
	}

	private Object invoke( Object proxy, Method method, Object[] arguments ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, arguments );
		}

		final RestMethod met = this.methods.get( method );

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
