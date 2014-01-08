
package ascelion.rest.bridge.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.googlecode.gentyref.GenericTypeReflector;

public class RestClientIH
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

	private final Set<Method> methods;

	private final WebTarget target;

	private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

	private final Collection<Cookie> cookies = new LinkedList<>();

	private final Form form = new Form();

	RestClientIH( Class cls, WebTarget target )
	{
		this.cls = cls;
		this.methods = methodsOf( cls );
		this.target = Util.addPathFromAnnotation( cls, target );
	}

	RestClientIH( Class cls, WebTarget target, Map<String, List<Object>> headers, Collection<Cookie> cookies, Form form )
	{
		this.cls = cls;
		this.methods = methodsOf( cls );
		this.target = target;

		this.headers.putAll( headers );
		this.cookies.addAll( cookies );
		this.form.asMap().putAll( form.asMap() );
	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args )
	throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, args );
		}

		if( this.methods.contains( method ) ) {
			return invokeInterfaceMethod( method, args );
		}

		throw new UnsupportedOperationException( "Could not handle method " + method );
	}

	private Class getReturnType( Method method )
	{
		final Type returnType = GenericTypeReflector.getExactReturnType( method, this.cls );

		if( returnType instanceof Class ) {
			return (Class) returnType;
		}

		throw new UnsupportedOperationException( "Return type is not a class: " + returnType );
	}

	private Object invokeInterfaceMethod( Method method, Object[] args )
	{
		final RestMethod request = new RestMethod( this.cls, method, this.target );

		return request.call( args, this.headers, this.cookies, this.form );
	}

}
