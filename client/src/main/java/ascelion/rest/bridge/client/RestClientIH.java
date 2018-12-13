
package ascelion.rest.bridge.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

final class RestClientIH
implements InvocationHandler
{

	static private final Collection<Method> O_METHODS = methodsOf( Object.class );

	static Collection<Method> methodsOf( Class cls )
	{
		return Arrays.asList( cls.getMethods() );
	}

	private final Client client;
	private final WebTarget target;
	private final Class cls;

	private final Map<Method, RestMethod> methods = new HashMap<>();
	private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
	private final Collection<Cookie> cookies = new ArrayList<>();
	private final Form form = new Form();

	RestClientIH( Client client, WebTarget target, Class cls, Map<String, List<Object>> headers, Collection<Cookie> cookies, Form form )
	{
		this( client, target, cls );

		this.headers.putAll( headers );
		this.cookies.addAll( cookies );
		this.form.asMap().putAll( form.asMap() );
	}

	RestClientIH( Client client, WebTarget target, Class cls )
	{
		this.client = client;
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
		this.methods.put( m, new RestMethod( this.cls, m, this.target ) );
	}

	private void initMethods()
	{
		for( final Method m : this.cls.getMethods() ) {
			addMethod( m );
		}
	}

	private Object invoke( Object proxy, RestMethod method, Object[] arguments ) throws URISyntaxException
	{
		final RestContext cx = new RestContext( proxy, method.method, arguments, this.client, method.target, this.headers, this.cookies, this.form );

		method.call( cx );

		if( cx.redirects > 0 ) {
			updateTarget( method, cx.target.getUri() );
		}

		return cx.result;
	}

	private void updateTarget( RestMethod restMethod, URI newTarget ) throws URISyntaxException
	{
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
