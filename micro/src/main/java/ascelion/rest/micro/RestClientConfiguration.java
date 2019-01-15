
package ascelion.rest.micro;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

public class RestClientConfiguration implements Configuration
{

	private static final Logger L = Logger.getLogger( "ascelion.rest.micro.config" );

	static private int getPriority( Class<?> cls )
	{
		return Optional.ofNullable( cls.getAnnotation( Priority.class ) )
			.map( Priority::value )
			.orElse( Priorities.USER );
	}

	private final Map<String, Object> properties = new HashMap<>();
	private final Set<Object> instances = new HashSet<>();
	private final Map<Class<?>, Map<Class<?>, Integer>> registrations = new IdentityHashMap<>();

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
		return isEnabled( feature.getClass() );
	}

	@Override
	public boolean isEnabled( Class<? extends Feature> featureClass )
	{
		return this.registrations.keySet().stream().anyMatch( featureClass::isAssignableFrom );
	}

	@Override
	public boolean isRegistered( Object component )
	{
		return this.instances.contains( component );
	}

	@Override
	public boolean isRegistered( Class<?> componentClass )
	{
		return this.registrations.containsKey( componentClass );
	}

	@Override
	public Map<Class<?>, Integer> getContracts( Class<?> componentClass )
	{
		return this.registrations.getOrDefault( componentClass, emptyMap() );
	}

	@Override
	public Set<Class<?>> getClasses()
	{
		return this.registrations.keySet();
	}

	@Override
	public Set<Object> getInstances()
	{
		return unmodifiableSet( this.instances );
	}

	void property( String name, Object value )
	{
		this.properties.put( name, value );
	}

	void addRegistration( Class<?> componentClass )
	{
		final Map<Class<?>, Integer> contracts = getAllInterfaces( componentClass ).stream()
			.collect( toMap( c -> c, x -> getPriority( x ) ) );

		this.registrations.put( componentClass, contracts );
	}

	void addRegistration( Class<?> componentClass, int priority )
	{
		final Map<Class<?>, Integer> contracts = getAllInterfaces( componentClass ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		addRegistration( componentClass, contracts );
	}

	void addRegistration( Class<?> componentClass, Class<?>[] contracts )
	{
		final Map<Class<?>, Integer> cm = Stream.of( contracts )
			.filter( c -> {
				if( c.isAssignableFrom( componentClass ) ) {
					return true;
				}

				L.warning( format( "Component %s is not assignable from %s", c.getName(), componentClass.getName() ) );

				return false;
			} )
			.collect( toMap( x -> x, x -> getPriority( x ) ) );

		addRegistration( componentClass, cm );
	}

	void addRegistration( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		this.registrations.put( componentClass, contracts );
	}

	void addInstance( Object instance )
	{
		this.instances.add( instance );
	}
}
