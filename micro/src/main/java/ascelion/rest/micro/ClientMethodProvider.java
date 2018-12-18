
package ascelion.rest.micro;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

final class ClientMethodProvider implements ClientRequestFilter
{

	static final ThreadLocal<Method> METHOD = new ThreadLocal<>();

	@Override
	public void filter( ClientRequestContext cx ) throws IOException
	{
		try {
			cx.setProperty( "org.eclipse.microprofile.rest.client.invokedMethod", METHOD.get() );
		}
		finally {
			METHOD.remove();
		}
	}
}
