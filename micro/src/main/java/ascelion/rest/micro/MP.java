
package ascelion.rest.micro;

import java.util.Optional;

import static java.lang.String.format;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class MP
{

	static public <T> Optional<T> getConfig( Class<?> service, Class<T> type, String what )
	{
		return getConfig( type, format( "%s/mp-rest/%s", service.getName(), what ) );
	}

	static public <T> Optional<T> getConfig( Class<T> type, String name )
	{
		return getConfig().map( c -> c.getOptionalValue( name, type ) ).orElse( Optional.empty() );
	}

	static public Optional<Config> getConfig()
	{
		try {
			return Optional.of( ConfigProvider.getConfig() );
		}
		catch( NoClassDefFoundError | ExceptionInInitializerError e ) {
			return Optional.empty();
		}
	}
}
