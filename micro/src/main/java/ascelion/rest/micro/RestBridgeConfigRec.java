
package ascelion.rest.micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

final class RestBridgeConfigRec implements Configurable<RestBridgeConfigRec>, Configuration
{

	@RequiredArgsConstructor
	static private class Record
	{

		final Object component;
		final Consumer<Configurable<?>> register;
	}

	private final Map<String, Object> properties = new LinkedHashMap<>();
	private final Collection<Record> records = new ArrayList<>();

	@Override
	public Configuration getConfiguration()
	{
		return this;
	}

	@Override
	public RestBridgeConfigRec property( String name, Object value )
	{
		this.properties.put( name, value );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Class<?> type )
	{
		this.records.add( new Record( type, c -> c.register( type ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Class<?> type, int priority )
	{
		this.records.add( new Record( type, c -> c.register( type, priority ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Class<?> type, Class<?>... contracts )
	{
		this.records.add( new Record( type, c -> c.register( type, contracts ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Class<?> type, Map<Class<?>, Integer> contracts )
	{
		this.records.add( new Record( type, c -> c.register( type, contracts ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Object component )
	{
		this.records.add( new Record( component, c -> c.register( component ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Object component, int priority )
	{
		this.records.add( new Record( component, c -> c.register( component, priority ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Object component, Class<?>... contracts )
	{
		this.records.add( new Record( component, c -> c.register( component, contracts ) ) );

		return this;
	}

	@Override
	public RestBridgeConfigRec register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.records.add( new Record( component, c -> c.register( component, contracts ) ) );

		return this;
	}

	@Override
	public RuntimeType getRuntimeType()
	{
		return RuntimeType.CLIENT;
	}

	@Override
	public Map<String, Object> getProperties()
	{
		return unmodifiableMap( this.properties );
	}

	@Override
	public Object getProperty( String name )
	{
		return this.properties.get( name );
	}

	@Override
	public Collection<String> getPropertyNames()
	{
		return this.properties.keySet();
	}

	@Override
	public boolean isEnabled( Feature feature )
	{
		return false;
	}

	@Override
	public boolean isEnabled( Class<? extends Feature> featureClass )
	{
		return false;
	}

	@Override
	public boolean isRegistered( Object component )
	{
		return isRegistered( component.getClass() );

	}

	@Override
	public boolean isRegistered( Class<?> type )
	{
		return this.records.stream()
			.anyMatch( r -> {
				if( r.component instanceof Class ) {
					return r.component.equals( type );
				}
				else {
					return type.isInstance( r.component );
				}
			} );
	}

	@Override
	public Map<Class<?>, Integer> getContracts( Class<?> type )
	{
		return emptyMap();
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public Set<Class<?>> getClasses()
	{
		return (Set) this.records.stream()
			.filter( Class.class::isInstance )
			.map( Class.class::cast )
			.collect( toSet() );
	}

	@Override
	public Set<Object> getInstances()
	{
		return emptySet();
	}

	RestBridgeConfiguration forClient( RestClientBuilder bld )
	{
		final RestBridgeConfiguration clone = new RestBridgeConfiguration( bld );

		this.properties.forEach( clone::property );
		this.records.forEach( r -> r.register.accept( clone ) );

		return clone;
	}
}
