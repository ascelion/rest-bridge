
package ascelion.rest.micro;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.utils.etc.Log;

import static ascelion.rest.bridge.client.RBUtils.newInstance;
import static java.util.Collections.emptySet;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@RequiredArgsConstructor( access = AccessLevel.PACKAGE )
final class RestBridgeConfiguration implements Configurable<RestBridgeConfiguration>, Configuration
{

	static final Log LOG = Log.get( "ascelion.rest.bridge.micro.CONFIG" );

	private static Map<Class<?>, Boolean> SUPPORTED; // V is true if supported by JAX-RS

	static {
		final Map<Class<?>, Boolean> map = new IdentityHashMap<>();

		map.put( Feature.class, true );
		map.put( ClientRequestFilter.class, true );
		map.put( ClientResponseFilter.class, true );
		map.put( ReaderInterceptor.class, true );
		map.put( WriterInterceptor.class, true );
		map.put( MessageBodyReader.class, true );
		map.put( MessageBodyWriter.class, true );
		map.put( ParamConverterProvider.class, true );
		map.put( ResponseExceptionMapper.class, false );
		map.put( AsyncInvocationInterceptorFactory.class, false );

		SUPPORTED = unmodifiableMap( map );
	}

	private final RestClientBuilder bld;
	private final Map<String, Object> properties = new HashMap<>();
	private final Map<Class<?>, Registration> registrations = new IdentityHashMap<>();
	private final Set<Class<?>> enabledFeatures = newSetFromMap( new IdentityHashMap<>() );

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
		return register( newInstance( type ) );
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, int priority )
	{
		return register( newInstance( type ), priority );
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, Class<?>... contracts )
	{
		return register( newInstance( type ), contracts );
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, Map<Class<?>, Integer> contracts )
	{
		return register( newInstance( type ), contracts );
	}

	@Override
	public RestBridgeConfiguration register( Object component )
	{
		final Class<?> type = component.getClass();

		if( this.registrations.containsKey( type ) ) {
			LOG.warn( "Component of type %s has been already registered", type.getName() );

			return this;
		}

		final int priority = component instanceof ResponseExceptionMapper
			? ( (ResponseExceptionMapper) component ).getPriority()
			: RBUtils.getPriority( type );
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
		final Registration reg = addRegistration( component, contracts );

		if( reg != null && reg.isFeature() ) {
			handleFeature( (Feature) reg.getInstance() );
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

	private Collection<Class<?>> lookupContracts( Class<?> type )
	{
		return getAllInterfaces( type ).stream()
			.filter( SUPPORTED::containsKey )
			.collect( toSet() );
	}

	private Registration addRegistration( Object instance, Map<Class<?>, Integer> contracts )
	{
		return this.registrations.compute( instance.getClass(), ( key, reg ) -> mergeContracts( instance, key, contracts, reg ) );
	}

	private Registration mergeContracts( Object instance, Class<?> type, Map<Class<?>, Integer> contracts, Registration reg )
	{
		final Map<Class<?>, Integer> map = new IdentityHashMap<>();

		for( final Map.Entry<Class<?>, Integer> e : contracts.entrySet() ) {
			final Class<?> c = e.getKey();

			if( SUPPORTED.containsKey( c ) ) {
				if( c.isAssignableFrom( type ) ) {
					LOG.debug( "Setting priority %d for contract %s of %s", e.getValue(), c.getName(), type.getName() );

					map.put( c, e.getValue() );
				}
				else {
					LOG.warn( "Component %s is not assignable from %s", type.getName(), type.getName() );
				}
			}
			else {
				LOG.warn( "Unsupported contract: %s", c.getName() );
			}
		}

		if( map.isEmpty() ) {
			LOG.warn( "Skipping registration of %s", type.getName() );

			return reg;
		}

		if( reg == null ) {
			reg = new Registration( instance, type, contracts );

			reg.injectAnnotated( Context.class, HttpHeaders.class, ThreadLocalProxy.create( HttpHeaders.class ) );

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

	RestBridgeConfiguration forJAXRS()
	{
		final RestBridgeConfiguration clone = new RestBridgeConfiguration( this.bld );

		clone.properties.putAll( this.properties );
		clone.enabledFeatures.addAll( this.enabledFeatures );

		this.registrations.forEach( ( type, reg ) -> {
			final Map<Class<?>, Integer> c = reg.getContracts()
				.entrySet().stream()
				.filter( e -> SUPPORTED.getOrDefault( e.getKey(), false ) )
				.collect( toMap( e -> e.getKey(), e -> e.getValue() ) );

			if( c.size() > 0 ) {
				clone.registrations.put( type, new Registration( reg.getInstance(), type, c ) );
			}
		} );

		return clone;
	}
}
