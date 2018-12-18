
package ascelion.rest.micro.cdi;

import java.util.Optional;

import static java.lang.String.format;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

final class MP
{

	static Optional<String> getConfig( Class<?> type, String what )
	{
		return getConfig( format( "%s/mp-rest/%s", type.getName(), what ) );
	}

	static private Optional<String> getConfig( String name )
	{
		return getConfig().map( c -> c.getOptionalValue( name, String.class ) ).orElse( Optional.empty() );
	}

	static private Optional<Config> getConfig()
	{
		try {
			return Optional.of( ConfigProvider.getConfig() );
		}
		catch( NoClassDefFoundError | ExceptionInInitializerError e ) {
			return Optional.empty();
		}
	}

	private MP()
	{
	}
}
