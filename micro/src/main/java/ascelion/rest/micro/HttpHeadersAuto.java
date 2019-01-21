
package ascelion.rest.micro;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

@ConstrainedTo( RuntimeType.SERVER )
@Priority( Integer.MIN_VALUE + 1000 )
public class HttpHeadersAuto implements ForcedAutoDiscoverable
{

	@Override
	public void configure( FeatureContext fc )
	{
		if( !fc.getConfiguration().isRegistered( HttpHeadersFilter.class ) ) {
			fc.register( HttpHeadersFilter.class );
		}
	}

}
