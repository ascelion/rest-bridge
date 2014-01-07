
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

	WebTarget target;

	final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

	final Collection<Cookie> cookies = new LinkedList<>();

	final Form form = new Form();

	String contentType;

	Object entity;

	Object value;

	Collection<String> accepts = new LinkedList<>();

	RestContext( WebTarget target, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		this.target = target;

		this.headers.putAll( headers );
		this.cookies.addAll( cookies );
		this.form.asMap().putAll( form.asMap() );
	}
}
