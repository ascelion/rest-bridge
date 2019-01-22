
package ascelion.rest.micro;

import java.util.Collection;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllClientHeadersFilter;
import org.junit.Test;

public class TypeDescTest
{

	@Test
	public void run() throws IllegalAccessException
	{
		final TypeDesc<ReturnWithAllClientHeadersFilter> desc = new TypeDesc<>( ReturnWithAllClientHeadersFilter.class );
		final Collection<PropDesc<?>> props = (Collection<PropDesc<?>>) readDeclaredField( desc, "props", true );

		assertThat( props, hasSize( 1 ) );
	}

}
