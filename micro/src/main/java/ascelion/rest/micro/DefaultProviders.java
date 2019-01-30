
package ascelion.rest.micro;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import ascelion.utils.jaxrs.JSR310ParamConverters;

class DefaultProviders implements Feature
{

	@Override
	public boolean configure( FeatureContext fcx )
	{
		fcx.register( new ClientMethodProvider(), Integer.MIN_VALUE );
		fcx.register( new JSR310ParamConverters(), Integer.MAX_VALUE );

		try {
			fcx.register( new JsonBProvider(), Integer.MAX_VALUE );
		}
		catch( final NoClassDefFoundError e ) {
			;
		}

		fcx.register( new JsonPProvider(), Integer.MAX_VALUE );
		fcx.register( new MBRWString(), Integer.MAX_VALUE );
		fcx.register( new MBRWBytes(), Integer.MAX_VALUE );
		fcx.register( new MBRWInputStream(), Integer.MAX_VALUE );
		fcx.register( new MBRWReader(), Integer.MAX_VALUE );
		fcx.register( new MBRWFile(), Integer.MAX_VALUE );
		fcx.register( new MBRWTextPlain(), Integer.MAX_VALUE );

		return true;
	}

}
