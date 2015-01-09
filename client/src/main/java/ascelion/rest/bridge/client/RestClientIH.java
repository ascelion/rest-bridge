
package ascelion.rest.bridge.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

final class RestClientIH
implements InvocationHandler
{

	static <T> T[] A( T... ts )
	{
		return ts;
	}

	static Set<Method> methodsOf( Class cls )
	{
		return Arrays.asList( cls.getMethods() ).stream().collect( Collectors.toSet() );
	}

	static <X> X newProxy( Class<X> cls, RestClientIH ih )
	{
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();

		return (X) Proxy.newProxyInstance( cld, A( cls ), ih );
	}

	static private final Set<Method> O_METHODS = methodsOf( Object.class );

	private final Class cls;

	private final WebTarget target;

	private final URI targetURI;

	private final Map<Method, RestMethod> methods = new HashMap<>();

	private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

	private final Collection<Cookie> cookies = new LinkedList<>();

	private final Form form = new Form();

	private final Client client;

	private final RestClient restClient;

	RestClientIH( Client client, Class cls, WebTarget target, UnaryOperator<Builder> onBuildRequest, Map<String, List<Object>> headers, Collection<Cookie> cookies, Form form )
	{
		this.restClient = null;
		this.client = client;
		this.cls = cls;
		this.target = target;
		this.targetURI = target.getUri();

		initMethods();

		this.headers.putAll( headers );
		this.cookies.addAll( cookies );
		this.form.asMap().putAll( form.asMap() );
	}

	RestClientIH( RestClient restClient, Class cls )
	{
		this.restClient = restClient;
		this.cls = cls;
		this.client = restClient.createClient();
		this.target = Util.addPathFromAnnotation( cls, this.client.target( restClient.target ) );
		this.targetURI = this.target.getUri();

		initMethods();

	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] arguments )
	throws Throwable
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
		return String.format( "%s -> %s", this.cls.getName(), this.targetURI );
	}

	void close()
	{
		if( this.restClient != null && this.client != null ) {
			this.client.close();
		}
	}

	private void addMethod( Method m )
	{
		this.methods.put( m, new RestMethod( this.cls, m, this.target ) );
	}

	private void initMethods()
	{
		Stream.of( this.cls.getMethods() ).forEach( this::addMethod );
	}

	private Object invoke( Object proxy, RestMethod restMethod, Object[] arguments )
	throws URISyntaxException
	{
		final RestContext cx = new RestContext( proxy, restMethod.method, arguments, this.client, restMethod.target, this.restClient.onNewRequest, this.headers, this.cookies, this.form );

		restMethod.call( cx );

		if( cx.redirects > 0 ) {
			updateTarget( restMethod, cx.target.getUri() );
		}

		return cx.result;
	}

	private void updateTarget( RestMethod restMethod, URI newTarget )
	throws URISyntaxException
	{
		//		final String path = restMethod.target.getUri().getPath();
		//		String newPath = newTarget.getPath();
		//
		//		newPath = newPath.substring( 0, newPath.indexOf( path ) + path.length() );
		//		newTarget = new URI( newTarget.getScheme(), newTarget.getAuthority(), newPath, null, null );
		//
		//		restMethod.target = this.client.target( newTarget );
	}
}
