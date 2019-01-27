
package ascelion.rest.micro;

import java.util.Collection;

import javax.ws.rs.core.Context;

import ascelion.utils.etc.PropDescriptor;
import ascelion.utils.etc.TypeDescriptor;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllClientHeadersFilter;
import org.junit.Test;

public class TypeDescTest
{

	@Test
	public void run() throws IllegalAccessException
	{
		final TypeDescriptor desc = new TypeDescriptor( ReturnWithAllClientHeadersFilter.class );
		final Collection<PropDescriptor<?>> props = desc.getProperties( Context.class );

		assertThat( props, hasSize( 1 ) );
	}

}
