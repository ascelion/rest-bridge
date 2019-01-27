
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class RestMethodTest
{

	private final MockClient mc = new MockClient();

	@Test
	public void missingPaths() throws NoSuchMethodException, SecurityException
	{

		final Method met = InterfaceWithMissingPath.class.getMethod( "get", String.class );
		final RestServiceInfo rsi = new RestServiceInfo( this.mc.rci, InterfaceWithMissingPath.class );
		final RestMethodInfo rmi = new RestMethodInfo( rsi, met );

		try {
			new RestMethod( rmi, this.mc.rci );
		}
		catch( final RestClientMethodException e ) {
			System.out.println( e.getMessage() );

			assertThat( e.getMethod(), sameInstance( met ) );
			assertThat( e.getMessage(), containsString( "{path1}" ) );
			assertThat( e.getMessage(), not( containsString( "{path2}" ) ) );
			assertThat( e.getMessage(), containsString( "{path3}" ) );
		}
	}

}
