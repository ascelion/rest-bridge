
package ascelion.rest.micro;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.AbstractMultivaluedMap;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

final class ROMultivaluedMap<K, V> extends AbstractMultivaluedMap<K, V>
{

	@SuppressWarnings( "rawtypes" )
	static final MultivaluedMap EMPTY = new ROMultivaluedMap<>();

	static <K, V> MultivaluedMap<K, V> empty()
	{
		return EMPTY;
	}

	private ROMultivaluedMap()
	{
		super( emptyMap() );
	}

	ROMultivaluedMap( Map<K, List<V>> store )
	{
		super( unmodifiableMap( store ) );
	}
}
