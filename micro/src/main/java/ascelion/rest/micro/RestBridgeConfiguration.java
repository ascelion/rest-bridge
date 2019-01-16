
package ascelion.rest.micro;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import ascelion.rest.bridge.client.RBUtils;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@RequiredArgsConstructor
final class RestBridgeConfiguration implements Configuration
{

	private static final Logger L = Logger.getLogger( "ascelion.rest.bridge.micro.CONFIG" );

	private final RestBridgeBuilder bld;
	private final Map<String, Object> properties = new HashMap<>();
	private final Set<Class<?>> classes = newSetFromMap( new IdentityHashMap<>() );
	private final Set<Object> instances = newSetFromMap( new IdentityHashMap<>() );
	private final Map<Class<?>, Map<Class<?>, Integer>> registrations = new IdentityHashMap<>();
	private final Set<Class<?>> enabledFeatures = newSetFromMap( new IdentityHashMap<>() );

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
		return this.enabledFeatures.contains( feature.getClass() );
	}

	@Override
	public boolean isEnabled( Class<? extends Feature> featureClass )
	{
		return this.enabledFeatures.contains( featureClass );
	}

	@Override
	public boolean isRegistered( Object component )
	{
		return this.instances.contains( component );
	}

	@Override
	public boolean isRegistered( Class<?> type )
	{
		return this.registrations.containsKey( type );
	}

	@Override
	public Map<Class<?>, Integer> getContracts( Class<?> type )
	{
		return this.registrations.getOrDefault( type, emptyMap() );
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

	RestBridgeConfiguration forClient( RestBridgeBuilder bld )
	{
		final RestBridgeConfiguration clone = new RestBridgeConfiguration( bld );

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

	void register( Class<?> type, int priority )
	{
		addClass( type, false );
		addRegistration( type, priority );
	}

	void addRegistration( Class<?> type )
	{
		final int priority = RBUtils.getPriority( type );
		final Map<Class<?>, Integer> cm = getAllInterfaces( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		addRegistration( type, cm );
	}

	void addRegistration( Class<?> type, int priority )
	{
		final Map<Class<?>, Integer> cm = getAllInterfaces( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		addRegistration( type, cm );
	}

	void addRegistration( Class<?> type, Class<?>[] contracts )
	{
		final Map<Class<?>, Integer> cm = Stream.of( contracts )
			.collect( toMap( x -> x, x -> RBUtils.getPriority( x ) ) );

		addRegistration( type, cm );
	}

	void addRegistration( Class<?> type, Map<Class<?>, Integer> contracts )
	{
		this.registrations.compute( type, ( key, map ) -> {
			if( map == null ) {
				map = new IdentityHashMap<>();
			}

			for( final Map.Entry<Class<?>, Integer> e : contracts.entrySet() ) {
				final Class<?> c = e.getKey();

				if( c.isAssignableFrom( type ) ) {
					L.fine( format( "Settiong priority %d for contract %s of %s", e.getValue(), c.getName(), type.getName() ) );

					map.put( c, e.getValue() );
				}
				else {
					L.warning( format( "Component %s is not assignable from %s", key.getName(), type.getName() ) );
				}
			}

			return map;
		} );
	}

	boolean addClass( Class<?> type, boolean warn )
	{
		if( !this.classes.add( type ) && warn ) {
			L.warning( format( "Component %s has been already registered", type.getName() ) );

			return false;
		}

		if( Feature.class.isAssignableFrom( type ) ) {
			handleFeature( (Feature) RBUtils.newInstance( type ) );
		}

		return true;
	}

	boolean addInstance( Object instance, boolean warn )
	{
		if( !this.instances.add( instance ) && warn ) {
			L.warning( format( "Component %s has been already registered", instance ) );

			return false;
		}

		if( Feature.class.isInstance( instance ) ) {
			handleFeature( (Feature) instance );
		}

		return true;
	}

	private void handleFeature( Feature feat )
	{
		if( !isEnabled( feat ) ) {
			final FeatureContext fc = new RestBridgeFeatureContext( this.bld );

			if( feat.configure( fc ) ) {
				this.enabledFeatures.add( feat.getClass() );
			}
		}
	}
}
