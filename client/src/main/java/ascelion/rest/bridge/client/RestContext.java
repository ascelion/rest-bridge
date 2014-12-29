
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.UnaryOperator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

final class RestContext
{

	final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

	final Collection<Cookie> cookies = new LinkedList<>();

	final Form form = new Form();

	final Object proxy;

	final Client client;

	final UnaryOperator<Builder> onNewRequest;

	final Method method;

	final Object[] arguments;

	String[] accepts;

	String contentType;

	Object entity;

	boolean entityPresent;

	Object parameterValue;

	Object result;

	WebTarget target;

	int redirects;

	RestContext( Object proxy, Method method, Object[] arguments, WebTarget target, Client client, UnaryOperator<Builder> onNewRequest, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		this.target = target;
		this.onNewRequest = onNewRequest;
		this.proxy = proxy;
		this.method = method;
		this.client = client;
		this.arguments = arguments == null ? new Object[0] : arguments;

		if( headers != null ) {
			this.headers.putAll( headers );
		}
		if( cookies != null ) {
			this.cookies.addAll( cookies );
		}
		if( form != null ) {
			this.form.asMap().putAll( form.asMap() );
		}
	}
}
