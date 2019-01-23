
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
final class RestRequestContextImpl implements RestRequestContext
{

	private final RestClientData rcd;
	private final Method javaMethod;
	private final Configuration configuration;
	@Setter
	private WebTarget target;
	@Getter( value = AccessLevel.NONE )
	private final Object proxy;
	private final List<Object> arguments;

	private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
	private final Collection<Cookie> cookies = new ArrayList<>();

	RestRequestContextImpl( RestClientData rcd, Method javaMethod, WebTarget target, Object proxy, Object[] arguments )
	{
		this.rcd = rcd;
		this.javaMethod = javaMethod;
		this.target = target;
		this.proxy = proxy;
		this.arguments = arguments != null ? asList( arguments ) : emptyList();
		this.configuration = target.getConfiguration();
	}

	@Override
	public Object getInterface()
	{
		return this.proxy;
	}

	@Override
	public Class<?> getInterfaceType()
	{
		return this.rcd.type;
	}

	@Override
	public Object getArgumentAt( int index )
	{
		return this.arguments.get( index );
	}

	@Override
	public <T> T getArgumentAt( Class<T> type, int index )
	{
		return type.cast( this.arguments.get( index ) );
	}

	@Override
	public <T> Function<T, String> getConverter( Class<T> type, Annotation[] annotations )
	{
		return this.rcd.cvsf.getConverter( type, annotations );
	}

	void header( String name, String value )
	{
		this.headers.add( name, value );
	}

	void matrix( String name, String value )
	{
		this.target = this.target.matrixParam( name, value );
	}

	void path( String name, String value )
	{
		this.target = this.target.resolveTemplate( name, value, true );
	}

	void query( String name, String value )
	{
		this.target = this.target.queryParam( name, value );
	}
}
