
package ascelion.rest.bridge.client;

import java.util.Collection;
import java.util.LinkedList;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

class RestContext
{

	final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

	final Collection<Cookie> cookies = new LinkedList<>();

	final Form form = new Form();

	final Object[] arguments;

	String[] accepts;

	String contentType;

	Object entity;

	boolean entityPresent;

	Object parameterValue;

	Object result;

	WebTarget target;

	final Object proxy;

	RestContext( WebTarget target, Object proxy, Object[] arguments, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		this.target = target;
		this.proxy = proxy;
		this.arguments = arguments == null ? new Object[0] : arguments;

		this.headers.putAll( headers );
		this.cookies.addAll( cookies );
		this.form.asMap().putAll( form.asMap() );
	}
}
