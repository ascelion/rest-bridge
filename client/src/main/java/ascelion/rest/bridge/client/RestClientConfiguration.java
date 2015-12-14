
package ascelion.rest.bridge.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author pappy
 */
public class RestClientConfiguration
{

	static public final int PRIORITY_FIRST = 1000;

	static public final int PRIORITY_LAST = 2000;

	static private int endPosition( String element )
	{
		return element.endsWith( "/" ) ? element.length() - 1 : element.length();
	}

	final String target;

	/**
	 * The number of connections allowed to pool.
	 *
	 */
	final int connectionPoolSize;

	/**
	 * The number of connections to pool per URL.
	 */
	final int maxPooledPerRoute;

	/**
	 * Set the time to live in the connection pool, in milliseconds.
	 */
	final int connectionTTL;

	final Map<Object, Integer> components = new LinkedHashMap<>();

	public RestClientConfiguration( String appLocation, String restBase, int connectionPoolSize, int maxPooledPerRoute, int connectionTTL )
	{
		final StringBuilder target = new StringBuilder();

		target.append( appLocation, 0, endPosition( appLocation ) );
		target.append( "/" );

		if( restBase.startsWith( "/" ) ) {
			target.append( restBase.substring( 1 ), 0, endPosition( restBase ) - 1 );
		}
		else {
			target.append( restBase, 0, endPosition( restBase ) );
		}

		this.target = target.toString();
		this.connectionPoolSize = connectionPoolSize;
		this.maxPooledPerRoute = maxPooledPerRoute;
		this.connectionTTL = connectionTTL;
	}

	public void register( Object component, int priority )
	{
		Objects.requireNonNull( component, "The component cannot be null" );

		removeMatching( component instanceof Class ? (Class) component : component.getClass() );

		this.components.put( component, priority );
	}

	public void register( Object component )
	{
		register( component, PRIORITY_LAST );
	}

	private void removeMatching( Class cls )
	{
		for( final Object o : this.components.keySet().toArray() ) {
			if( cls.equals( o.getClass() ) || cls.equals( o ) ) {
				this.components.remove( o );
			}
		}
	}
}
