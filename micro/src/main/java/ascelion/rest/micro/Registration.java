
package ascelion.rest.micro;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.HttpHeaders;

import ascelion.utils.etc.TypeDescriptor;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import lombok.AccessLevel;
import lombok.Getter;

final class Registration
{

	static final Registration NONE = new Registration( null, Void.class, emptyMap() );

	@Getter( AccessLevel.PACKAGE )
	private final Object instance;
	private final Class<?> type;
	private final TypeDescriptor desc;
	private final Map<Class<?>, Integer> contracts;

	Registration( Object instance, Class<?> type, Map<Class<?>, Integer> contracts )
	{
		this.type = type;
		this.instance = instance;
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
		this.desc.injectAnnotated( annoType, propType, this.instance, ThreadLocalProxy.create( HttpHeaders.class ) );
	}
}
