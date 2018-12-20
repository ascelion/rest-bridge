
package ascelion.rest.micro;

import java.util.Collection;
import java.util.HashMap;
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
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

final class RestBridgeConfiguration implements Configuration
{

	private static final Logger L = Logger.getLogger( "ascelion.rest.micro.configuration" );

	static private int getPriority( Class<?> cls )
	{
		return Optional.ofNullable( cls.getAnnotation( Priority.class ) )
			.map( Priority::value )
			.orElse( Priorities.USER );
	}

	private final Map<String, Object> properties = new HashMap<>();
	private final Set<Class<?>> classes = newSetFromMap( new IdentityHashMap<>() );
	private final Set<Object> instances = newSetFromMap( new IdentityHashMap<>() );
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
		return false;
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
		return unmodifiableSet( this.classes );
	}

	@Override
	public Set<Object> getInstances()
	{
		return unmodifiableSet( this.instances );
	}

	Configuration forClient()
	{
		final RestBridgeConfiguration clone = new RestBridgeConfiguration();

		clone.properties.putAll( this.properties );

		this.classes.stream()
			.filter( t -> !ResponseExceptionMapper.class.isAssignableFrom( t ) )
			.forEach( clone.classes::add );
		this.instances.stream()
			.filter( t -> !ResponseExceptionMapper.class.isInstance( t ) )
			.forEach( clone.instances::add );
		this.registrations.entrySet().stream()
			.filter( e -> !ResponseExceptionMapper.class.isAssignableFrom( e.getKey() ) )
			.forEach( e -> clone.registrations.put( e.getKey(), e.getValue() ) );

		return clone;
	}

	void property( String name, Object value )
	{
		this.properties.put( name, value );
	}

	boolean addRegistration( Class<?> componentClass )
	{
		if( this.registrations.containsKey( componentClass ) ) {
			L.warning( format( "Component %s has been already registered", componentClass.getName() ) );

			return false;
		}

		final int priority = getPriority( componentClass );
		final Map<Class<?>, Integer> cm = getAllInterfaces( componentClass ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		return doRegistration( componentClass, cm );
	}

	boolean addRegistration( Class<?> componentClass, int priority )
	{
		if( this.registrations.containsKey( componentClass ) ) {
			L.warning( format( "Component %s has been already registered", componentClass.getName() ) );

			return false;
		}

		final Map<Class<?>, Integer> cm = getAllInterfaces( componentClass ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		return doRegistration( componentClass, cm );
	}

	boolean addRegistration( Class<?> componentClass, Class<?>[] contracts )
	{
		if( this.registrations.containsKey( componentClass ) ) {
			L.warning( format( "Component %s has been already registered", componentClass.getName() ) );

			return false;
		}

		final Map<Class<?>, Integer> cm = Stream.of( contracts )
			.collect( toMap( x -> x, x -> getPriority( x ) ) );

		return doRegistration( componentClass, cm );
	}

	boolean addRegistration( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		if( this.registrations.containsKey( componentClass ) ) {
			L.warning( format( "Component %s has been already registered", componentClass.getName() ) );

			return false;
		}

		return doRegistration( componentClass, contracts );
	}

	void addClass( Class<?> type )
	{
		this.classes.add( type );
	}

	void addInstance( Object instance )
	{
		this.instances.add( instance );
	}

	private boolean doRegistration( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		final Map<Class<?>, Integer> mc = contracts.entrySet().stream()
			.filter( e -> {
				final Class<?> c = e.getKey();

				if( c.isAssignableFrom( componentClass ) ) {
					return true;
				}

				L.warning( format( "Component %s is not assignable from %s", c.getName(), componentClass.getName() ) );

				return false;
			} )
			.collect( toMap( Map.Entry::getKey, Map.Entry::getValue ) );

		if( mc.isEmpty() ) {
			L.warning( format( "Abandoned registration of %s", componentClass.getName() ) );

			return false;
		}

		this.registrations.put( componentClass, mc );

		return true;
	}
}
