
package ascelion.rest.micro;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.HttpHeaders;

import ascelion.rest.bridge.client.Prioritised;
import ascelion.utils.etc.TypeDescriptor;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import lombok.ToString;

@ToString
final class Registration<T> extends Prioritised<T>
{

	static final Registration<?> NONE = new Registration<>( -1, null, Void.class, emptyMap() );

	private final Class<?> type;
	private final TypeDescriptor desc;
	private final Map<Class<?>, Integer> contracts;

	Registration( int index, T instance, Class<?> type, Map<Class<?>, Integer> contracts )
	{
		super( index, instance );

		this.type = type;
		this.desc = new TypeDescriptor( type );
		this.contracts = contracts;
	}

	Map<Class<?>, Integer> getContracts()
	{
		return unmodifiableMap( this.contracts );
	}

	void add( Map<Class<?>, Integer> contracts )
	{
		this.contracts.putAll( contracts );
	}

	boolean isFeature()
	{
		return Feature.class.isAssignableFrom( this.type );
	}

	void injectAnnotated( Class<? extends Annotation> annoType, Class<HttpHeaders> propType, Object value )
	{
		this.desc.injectAnnotated( annoType, propType, getInstance(), ThreadLocalProxy.create( HttpHeaders.class ) );
	}
}
