
package ascelion.rest.bridge.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

final class RestClientIH
implements InvocationHandler
{

	static private final Collection<Method> O_METHODS = methodsOf( Object.class );

	static Collection<Method> methodsOf( Class cls )
	{
		return Arrays.asList( cls.getMethods() );
	}

	private final Client client;
	private final ConvertersFactory cvsf;
	private final WebTarget target;
	private final Class cls;

	private final Map<Method, RestMethod> methods = new HashMap<>();

	RestClientIH( Client client, WebTarget target, Class cls )
	{
		this.client = client;
		this.cvsf = new ConvertersFactory( client.getConfiguration() );
		this.target = target;
		this.cls = cls;

		initMethods();

	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] arguments ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, arguments );
		}

		final RestMethod rest = this.methods.get( method );

		if( rest != null ) {
			return invoke( proxy, rest, arguments );
		}

		throw new UnsupportedOperationException( "Could not handle method " + method );
	}

	@Override
	public String toString()
	{
		return String.format( "%s -> %s", this.cls.getName(), this.target );
	}

	private void addMethod( Method m )
	{
		this.methods.put( m, new RestMethod( this.cvsf, this.cls, m, this.target ) );
	}

	private void initMethods()
	{
		for( final Method m : this.cls.getMethods() ) {
			addMethod( m );
		}
	}

	private Object invoke( Object proxy, RestMethod method, Object[] arguments ) throws URISyntaxException
	{
		final RestRequest cx = new RestRequest( proxy, this.client, method.target, arguments );

		method.call( cx );

		if( cx.redirects > 0 ) {
			updateTarget( method, cx.target.getUri() );
		}

		return cx.result;
	}

	private void updateTarget( RestMethod restMethod, URI newTarget ) throws URISyntaxException
	{
		throw new UnsupportedOperationException( "TODO" );
		//		final String path = restMethod.target.getUri().getPath();
		//		String newPath = newTarget.getPath();
		//
		//		newPath = newPath.substring( 0, newPath.indexOf( path ) + path.length() );
		//		newTarget = new URI( newTarget.getScheme(), newTarget.getAuthority(), newPath, null, null );
		//
		//		restMethod.target = this.client.target( newTarget );
	}

	<X> X newProxy()
	{
		return (X) Proxy.newProxyInstance( this.cls.getClassLoader(), new Class[] { this.cls }, this );
	}
}
