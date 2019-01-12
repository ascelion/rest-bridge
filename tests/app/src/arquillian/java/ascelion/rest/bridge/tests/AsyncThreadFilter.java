
package ascelion.rest.bridge.tests;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class AsyncThreadFilter implements ClientRequestFilter
{

	static private volatile long threadId;

	@Override
	public void filter( ClientRequestContext requestContext ) throws IOException
	{
		threadId = currentThread().getId();
	}

	public static void checkThread()
	{
		assertThat( threadId, not( equalTo( currentThread().getId() ) ) );
	}

	public static void reset()
	{
		threadId = currentThread().getId();
	}

}
