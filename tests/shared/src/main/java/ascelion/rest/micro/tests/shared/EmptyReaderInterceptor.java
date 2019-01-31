
package ascelion.rest.micro.tests.shared;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

public class EmptyReaderInterceptor implements ReaderInterceptor
{

	static public volatile boolean invoked;

	@Override
	public Object aroundReadFrom( ReaderInterceptorContext context ) throws IOException, WebApplicationException
	{
		invoked = true;

		return context.proceed();
	}

}
