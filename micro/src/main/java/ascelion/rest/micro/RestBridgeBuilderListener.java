
package ascelion.rest.micro;

import ascelion.utils.jaxrs.JSR310ParamConverters;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;

public class RestBridgeBuilderListener implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder bld )
	{
		bld.register( new ClientMethodProvider(), Integer.MIN_VALUE );
		bld.register( new JSR310ParamConverters(), Integer.MAX_VALUE );

		try {
			bld.register( new JsonBProvider(), Integer.MAX_VALUE );
		}
		catch( final NoClassDefFoundError e ) {
			;
		}

		bld.register( new JsonPProvider(), Integer.MAX_VALUE );
		bld.register( new MBRWString(), Integer.MAX_VALUE );
		bld.register( new MBRWBytes(), Integer.MAX_VALUE );
		bld.register( new MBRWInputStream(), Integer.MAX_VALUE );
		bld.register( new MBRWReader(), Integer.MAX_VALUE );
		bld.register( new MBRWFile(), Integer.MAX_VALUE );
		bld.register( new MBRWTextPlain(), Integer.MAX_VALUE );
	}
}
