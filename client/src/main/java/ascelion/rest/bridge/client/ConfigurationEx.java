
package ascelion.rest.bridge.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configuration;

import static java.util.stream.Collectors.toList;

public interface ConfigurationEx
{

	static <T> List<Prioritised<T>> providers( Configuration cf, Class<T> type )
	{
		if( cf instanceof ConfigurationEx ) {
			return ( (ConfigurationEx) cf ).providers( type );
		}
		else {
			final Map<Class<T>, T> result = new LinkedHashMap<>();

			cf.getInstances().stream()
				.filter( type::isInstance )
				.map( type::cast )
				.forEach( p -> result.put( (Class<T>) p.getClass(), p ) );

			cf.getClasses().stream()
				.filter( t -> !result.containsKey( t ) )
				.filter( type::isAssignableFrom )
				.map( RBUtils::newInstance )
				.map( type::cast )
				.forEach( p -> result.put( (Class<T>) p.getClass(), p ) );

			return result.values().stream()
				.map( p -> new Prioritised<>( cf.getContracts( p.getClass() ).getOrDefault( type, Priorities.USER ), p ) )
				.sorted()
				.collect( toList() );
		}
	}

	<T> List<Prioritised<T>> providers( Class<T> type );
}
