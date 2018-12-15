
package ascelion.rest.bridge.client;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

final class RestRequest
{

	final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
	final Collection<Cookie> cookies = new ArrayList<>();
	final Object proxy;
	final Client client;
	final Object[] arguments;
	String[] accepts;
	String contentType;
	Object entity;
	Object result;
	WebTarget target;
	int redirects;

	RestRequest( Object proxy, Client client, WebTarget target, Object... arguments )
	{
		this.target = target;
		this.proxy = proxy;
		this.client = client;
		this.arguments = arguments == null ? new Object[0] : arguments;
	}
}
