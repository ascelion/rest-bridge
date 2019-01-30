
package ascelion.rest.micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

import ascelion.rest.bridge.client.ConfigurationEx;
import ascelion.rest.bridge.client.Prioritised;
import ascelion.rest.bridge.client.RBUtils;
import ascelion.utils.etc.Log;

import static ascelion.rest.bridge.client.RBUtils.newInstance;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

final class RestBridgeConfiguration implements Configurable<RestBridgeConfiguration>, Configuration, ConfigurationEx
{

	static final Log LOG = Log.get( "ascelion.rest.bridge.micro.CONFIG" );

	static final Map<Class<?>, Boolean> SUPPORTED; // V is true if supported by JAX-RS

	static {
		final Map<Class<?>, Boolean> map = new LinkedHashMap<>();

		map.put( ParamConverterProvider.class, true );
		map.put( AsyncInvocationInterceptorFactory.class, false );
		map.put( ClientRequestFilter.class, true );
		map.put( ClientResponseFilter.class, true );
		map.put( ReaderInterceptor.class, true );
		map.put( WriterInterceptor.class, true );
		map.put( MessageBodyReader.class, true );
		map.put( MessageBodyWriter.class, true );
		map.put( ResponseExceptionMapper.class, false );

		SUPPORTED = unmodifiableMap( map );
	}

	private final Map<String, Object> properties = new HashMap<>();
	private final Map<Class<?>, Registration<?>> registrations = new LinkedHashMap<>();
	private final Set<Class<?>> enabledFeatures = newSetFromMap( new LinkedHashMap<>() );
	private final Collection<Runnable> changes = new ArrayList<>();
	private final Map<Class<?>, List<Prioritised<?>>> providers = new IdentityHashMap<>();

	@Override
	public RestBridgeConfiguration getConfiguration()
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
		this.changes.add( () -> doRegister( type ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, int priority )
	{
		this.changes.add( () -> doRegister( type, priority ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, Class<?>... contracts )
	{
		this.changes.add( () -> doRegister( type, contracts ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Class<?> type, Map<Class<?>, Integer> contracts )
	{
		this.changes.add( () -> doRegister( type, contracts ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Object component )
	{
		this.changes.add( () -> doRegister( component ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Object component, int priority )
	{
		this.changes.add( () -> doRegister( component, priority ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Object component, Class<?>... contracts )
	{
		this.changes.add( () -> doRegister( component, contracts ) );

		return this;
	}

	@Override
	public RestBridgeConfiguration register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.changes.add( () -> doRegister( component, contracts ) );

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
		applyChanges();

		return this.enabledFeatures.contains( feature.getClass() );
	}

	@Override
	public boolean isEnabled( Class<? extends Feature> featureClass )
	{
		applyChanges();

		return this.enabledFeatures.contains( featureClass );
	}

	@Override
	public boolean isRegistered( Object component )
	{
		applyChanges();

		return component.equals( this.registrations.getOrDefault( component.getClass(), Registration.NONE ).getInstance() );
	}

	@Override
	public boolean isRegistered( Class<?> type )
	{
		applyChanges();

		return this.registrations.containsKey( type );
	}

	@Override
	public Map<Class<?>, Integer> getContracts( Class<?> type )
	{
		applyChanges();

		return this.registrations.getOrDefault( type, Registration.NONE ).getContracts();
	}

	@Override
	public Set<Class<?>> getClasses()
	{
		applyChanges();

		return emptySet();
	}

	@Override
	public Set<Object> getInstances()
	{
		applyChanges();

		return unmodifiableSet( this.registrations.values().stream()
			.map( Prioritised::getInstance )
			.map( Objects::requireNonNull )
			.collect( toSet() ) );
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public <T> List<Prioritised<T>> providers( Class<T> type )
	{
		applyChanges();

		final List result = this.providers.getOrDefault( type, emptyList() ).stream()
			.map( p -> new Prioritised( p.getPriority() >> 32, p.getInstance() ) )
			.collect( toList() );

		return unmodifiableList( result );
	}

	public RestBridgeConfiguration clone( boolean forJAXRS )
	{
		applyChanges();

		final RestBridgeConfiguration clone = new RestBridgeConfiguration();

		clone.changes.addAll( this.changes );
		clone.properties.putAll( this.properties );
		clone.enabledFeatures.addAll( this.enabledFeatures );

		this.registrations.forEach( ( type, reg ) -> {
			final Map<Class<?>, Integer> cts = reg.getContracts()
				.entrySet().stream()
				.filter( e -> {
					final boolean s = SUPPORTED.getOrDefault( e.getKey(), false );

					return !forJAXRS || s;
				} )
				.collect( toMap( e -> e.getKey(), e -> e.getValue() ) );

			if( cts.size() > 0 ) {
				clone.registrations.put( type, new Registration<>( clone.registrations.size(), reg.getInstance(), type, cts ) );
			}
		} );

		clone.updateProviders();

		return clone;
	}

	void applyChanges()
	{
		while( this.changes.size() > 0 ) {
			final List<Runnable> tmp = new ArrayList<>( this.changes );

			this.changes.clear();

			tmp.forEach( Runnable::run );
		}
	}

	void doProperty( String name, Object value )
	{
		this.properties.put( name, value );
	}

	void doRegister( Class<?> type )
	{
		doRegister( newInstance( type ) );
	}

	void doRegister( Class<?> type, int priority )
	{
		doRegister( newInstance( type ), priority );
	}

	void doRegister( Class<?> type, Class<?>... contracts )
	{
		doRegister( newInstance( type ), contracts );
	}

	void doRegister( Class<?> type, Map<Class<?>, Integer> contracts )
	{
		doRegister( newInstance( type ), contracts );
	}

	void doRegister( Object component )
	{
		final Class<?> type = component.getClass();

		if( this.registrations.containsKey( type ) ) {
			LOG.warn( "Component of type %s has been already registered", type.getName() );

			return;
		}

		final int priority = component instanceof ResponseExceptionMapper
			? ( (ResponseExceptionMapper<?>) component ).getPriority()
			: RBUtils.getPriority( type );
		final Map<Class<?>, Integer> cm = lookupContracts( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		doRegister( component, cm );
	}

	void doRegister( Object component, int priority )
	{
		final Class<?> type = component.getClass();
		final Map<Class<?>, Integer> cm = lookupContracts( type ).stream()
			.collect( toMap( x -> x, x -> priority ) );

		doRegister( component, cm );
	}

	void doRegister( Object component, Class<?>... contracts )
	{
		final int priority = RBUtils.getPriority( component );
		final Map<Class<?>, Integer> cm = Stream.of( contracts )
			.collect( toMap( x -> x, x -> priority ) );

		doRegister( component, cm );
	}

	void doRegister( Object component, Map<Class<?>, Integer> contracts )
	{
		addRegistration( component, contracts );
	}

	private Collection<Class<?>> lookupContracts( Class<?> type )
	{
		return getAllInterfaces( type ).stream()
			.filter( SUPPORTED::containsKey )
			.collect( toSet() );
	}

	private void addRegistration( Object component, Map<Class<?>, Integer> contracts )
	{
		final Class<? extends Object> type = component.getClass();

		if( Feature.class.isAssignableFrom( type ) ) {
			if( this.enabledFeatures.contains( type ) ) {
				return;
			}

			if( contracts.size() > 0 ) {
				LOG.warn( "Contracts ignored for feature %s", type.getName() );

				return;
			}

			final Feature ft = (Feature) component;
			final FeatureContext fc = new RestBridgeFeatureContext( this );

			if( ft.configure( fc ) ) {
				this.enabledFeatures.add( type );
			}
		}
		else {
			try {
				final Map<Class<?>, Integer> cts = contracts;

				this.registrations.compute( component.getClass(), ( key, reg ) -> mergeContracts( component, key, cts, reg ) );
			}
			finally {
				updateProviders();
			}
		}
	}

	boolean isMBRW( Class<?> type )
	{
		return MessageBodyReader.class.equals( type ) || MessageBodyWriter.class.equals( type );
	}

	private <X> Registration<X> mergeContracts( X instance, Class<?> type, Map<Class<?>, Integer> contracts, Registration<?> reg )
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
			LOG.warn( "Skipping registration (no contract) of %s", type.getName() );

			return (Registration<X>) reg;
		}

		if( reg == null ) {
			reg = new Registration<>( this.registrations.size(), instance, type, contracts );

			reg.injectAnnotated( Context.class, HttpHeaders.class, ThreadLocalProxy.create( HttpHeaders.class ) );

		}
		else {
			reg.add( map );
		}

		return (Registration<X>) reg;
	}

	private void updateProviders()
	{
		this.providers.clear();

		this.registrations.forEach( ( type, reg ) -> {
			reg.getContracts().forEach( ( t, p ) -> {
				this.providers.computeIfAbsent( t, k -> new ArrayList<>() )
					.add( new Prioritised<>( (long) p << 32 | reg.getPriority(), reg.getInstance() ) );
			} );
		} );

		this.providers.forEach( ( t, c ) -> sort( (List) c ) );
	}

}
