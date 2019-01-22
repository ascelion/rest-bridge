
package ascelion.rest.micro;

import java.util.Map;

import javax.ws.rs.core.Feature;

import ascelion.rest.bridge.client.RBUtils;

import static java.util.Collections.emptyMap;

import lombok.AccessLevel;
import lombok.Getter;

@Getter( AccessLevel.PACKAGE )
final class Registration<T>
{

	static final Registration<?> NONE = new Registration<>( Void.class, emptyMap(), null );

	private final TypeDesc<T> desc;
	private final Map<Class<?>, Integer> contracts;
	private T instance;

	Registration( Class<T> type, Map<Class<?>, Integer> contracts, T instance )
	{
		this.desc = new TypeDesc<>( type );
		this.contracts = contracts;
		this.instance = instance;
	}

	boolean updateInstance( T instance )
	{
		if( this.instance != null ) {
			return false;
		}

		if( instance == null ) {
			instance = RBUtils.newInstance( this.desc.type );
		}

		this.desc.inject( instance );

		this.instance = instance;

		return true;
	}

	void add( Map<Class<?>, Integer> contracts )
	{
		this.contracts.putAll( contracts );
	}

	boolean isFeature()
	{
		return Feature.class.isAssignableFrom( this.desc.type );
	}
}
