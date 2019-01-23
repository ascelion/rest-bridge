
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;

public interface RestRequestContext
{

	Configuration getConfiguration();

	Object getInterface();

	Class<?> getInterfaceType();

	Object getArgumentAt( int index );

	<T> T getArgumentAt( Class<T> type, int index );

	Method getJavaMethod();

	WebTarget getTarget();

	MultivaluedMap<String, String> getHeaders();

	Collection<Cookie> getCookies();
}
