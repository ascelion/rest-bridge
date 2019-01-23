
package ascelion.rest.micro;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import ascelion.rest.bridge.client.RBUtils;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@RequiredArgsConstructor( access = AccessLevel.PRIVATE )
final class RestBridgeConfiguration implements Configurable<RestBridgeConfiguration>, Configuration
{

	static final Logger LOG = Logger.getLogger( "ascelion.rest.bridge.micro.CONFIG" );

	private static Set<Class<?>> SUPPORTED;

	static {
		final Set<Class<?>> map = newSetFromMap( new IdentityHashMap<>() );

		map.add( Feature.class );
		map.add( ClientRequestFilter.class );
		map.add( ClientResponseFilter.class );
		map.add( ReaderInterceptor.class );
		map.add( WriterInterceptor.class );
		map.add( MessageBodyReader.class );
		map.add( MessageBodyWriter.class );
		map.add( ParamConverterProvider.class );
		map.add( ResponseExceptionMapper.class );
		map.add( AsyncInvocationInterceptorFactory.class );

		SUPPORTED = unmodifiableSet( map );
	}

	private final RestBridgeBuilder bld;
	private final boolean autoCreate;
	private final Map<String, Object> properties = new HashMap<>();
	private final Map<Class<?>, Registration<?>> registrations = new IdentityHashMap<>();
	private final Set<Class<?>> enabledFeatures = newSetFromMap( new IdentityHashMap<>() );

	RestBridgeConfiguration( RestBridgeBuilder bld )
	{
		this( bld, false );
	}

	@Override
	public Configuration getConfiguration()
	{
		return this;
	}

	@Override
	public RestBridgeConfiguration property( String name, Object value )
	{
		this.properties.put( name, value );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type )
	{
		if( this.registrations.containsKey( type ) ) {
			LOG.warning( format( "Component of type %s has been already registered", type.getName() ) );

			return this;
		}

		final int priority = RBUtils.getPriority( type );
		final Map<Class<?>, Integer> cm = lookupContracts( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		return register( type, cm );
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, int priority )
	{
		final Map<Class<?>, Integer> cm = lookupContracts( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		return register( type, cm );
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, Class<?>... contracts )
	{
		final int priority = RBUtils.getPriority( type );
		final Map<Class<?>, Integer> cm = Stream.of( contracts )
			.collect( toMap( x -> x, x -> priority ) );

		return register( type, cm );
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, Map<Class<?>, Integer> contracts )
	{
		final Registration<?> reg = addRegistration( type, contracts );

		if( reg != null && this.autoCreate ) {
			if( reg.updateInstance( null ) && reg.isFeature() ) {
				handleFeature( (Feature) reg.getInstance() );
			}
		}

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Object component )
	{
		final Class<?> type = component.getClass();

		if( this.registrations.containsKey( type ) ) {
			LOG.warning( format( "Component of type %s has been already registered", type.getName() ) );

			return this;
		}

		final int priority = RBUtils.getPriority( type );
		final Map<Class<?>, Integer> cm = lookupContracts( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		return register( component, cm );
	}

	@Override
	public RestBridgeConfiguration register( Object component, int priority )
	{
		final Class<?> type = component.getClass();
		final Map<Class<?>, Integer> cm = lookupContracts( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		return register( component, cm );
	}

	@Override
	public RestBridgeConfiguration register( Object component, Class<?>... contracts )
	{
		final int priority = RBUtils.getPriority( component );
		final Map<Class<?>, Integer> cm = Stream.of( contracts )
			.collect( toMap( x -> x, x -> priority ) );

		return register( component, cm );
	}

	@Override
	public RestBridgeConfiguration register( Object component, Map<Class<?>, Integer> contracts )
	{
		final Class<? extends Object> type = component.getClass();
		final Registration<Object> reg = addRegistration( type, contracts );

		if( reg != null ) {
			if( reg.updateInstance( component ) && reg.isFeature() ) {
				handleFeature( (Feature) reg.getInstance() );
			}
		}

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
		return component.equals( this.registrations.getOrDefault( component.getClass(), Registration.NONE ).getInstance() );
	}

	@Override
	public boolean isRegistered( Class<?> type )
	{
		return this.registrations.containsKey( type );
	}

	@Override
	public Map<Class<?>, Integer> getContracts( Class<?> type )
	{
		return this.registrations.getOrDefault( type, Registration.NONE ).getContracts();
	}

	@Override
	public Set<Class<?>> getClasses()
	{
		return emptySet();
	}

	@Override
	public Set<Object> getInstances()
	{
		return unmodifiableSet( this.registrations.values().stream()
			.map( Registration::getInstance )
			.map( Objects::requireNonNull )
			.collect( toSet() ) );
	}

	RestBridgeConfiguration forClient()
	{
		final RestBridgeConfiguration clone = new RestBridgeConfiguration( this.bld, true );

		clone.properties.putAll( this.properties );
		clone.registrations.putAll( this.registrations );
		clone.registrations.values().forEach( reg -> {
			if( reg.updateInstance( null ) && reg.isFeature() ) {
				clone.handleFeature( (Feature) reg.getInstance() );
			}
		} );

		return clone;
	}

	private Collection<Class<?>> lookupContracts( Class<?> type )
	{
		return getAllInterfaces( type ).stream()
			.filter( SUPPORTED::contains )
			.collect( toSet() );
	}

	private <X> Registration<X> addRegistration( Class<? extends X> type, Map<Class<?>, Integer> contracts )
	{
		return (Registration<X>) this.registrations.compute( type, ( key, reg ) -> mergeContracts( key, reg, contracts ) );
	}

	private Registration<?> mergeContracts( Class<?> type, Registration<?> reg, Map<Class<?>, Integer> contracts )
	{
		final Map<Class<?>, Integer> map = new IdentityHashMap<>();

		for( final Map.Entry<Class<?>, Integer> e : contracts.entrySet() ) {
			final Class<?> c = e.getKey();

			if( SUPPORTED.contains( c ) ) {
				if( c.isAssignableFrom( type ) ) {
					LOG.fine( format( "Setting priority %d for contract %s of %s", e.getValue(), c.getName(), type.getName() ) );

					map.put( c, e.getValue() );
				}
				else {
					LOG.warning( format( "Component %s is not assignable from %s", type.getName(), type.getName() ) );
				}
			}
			else {
				LOG.warning( format( "Unsupported contract: %s", c.getName() ) );
			}
		}

		if( map.isEmpty() ) {
			LOG.warning( format( "Skipping registration of %s", type.getName() ) );

			return reg;
		}

		if( reg == null ) {
			reg = new Registration<>( type, contracts, null );
		}
		else {
			reg.add( map );
		}

		return reg;
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
