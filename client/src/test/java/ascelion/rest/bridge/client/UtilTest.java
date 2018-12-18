
package ascelion.rest.bridge.client;

import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class UtilTest
{

	@Test
	public void pathElements()
	{
		final Set<String> elements = Util.pathElements( "first/{path1}/{path2}/samd/{path3}/last" );

		assertThat( elements, hasSize( 3 ) );
		assertThat( elements, contains( "path1", "path2", "path3" ) );
	}

}
