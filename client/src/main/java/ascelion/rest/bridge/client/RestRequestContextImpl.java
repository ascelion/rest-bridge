
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

final class RestRequestContextImpl implements RestRequestContext
{

	final RestClientData rcd;
	@Getter
	private final Method javaMethod;
	@Getter
	private final Configuration configuration;
	@Getter
	@Setter
	private WebTarget target;
	@Getter( value = AccessLevel.NONE )
	final Object proxy;
	private final List<Object> arguments;
	private final GenericType<?> returnType;

	@Getter
	private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
	@Getter
	private final Collection<Cookie> cookies = new ArrayList<>();
	final Collection<MediaType> produces = new ArrayList<>();
	final Collection<MediaType> consumes = new ArrayList<>();
	Object entity;
	@Getter
	private final boolean async;
	@Getter
	private final String httpMethod;

	RestRequestContextImpl( RestClientData rcd, Method javaMethod, GenericType<?> returnType, boolean async, String httpMethod, WebTarget target, Object proxy, Object[] arguments )
	{
		this.rcd = rcd;
		this.javaMethod = javaMethod;
		this.target = target;
		this.proxy = proxy;
		this.arguments = arguments != null ? asList( arguments ) : emptyList();
		this.configuration = rcd.conf;
		this.returnType = returnType;
		this.async = async;
		this.httpMethod = httpMethod;
	}

	@Override
	public Object getImplementation()
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
	public <T> GenericType<T> getReturnType()
	{
		return (GenericType<T>) this.returnType;
	}

	@Override
	public Object[] getArguments()
	{
		return this.arguments.toArray();
	}

	@Override
	public <T> Function<T, String> getConverter( Class<T> type, Annotation[] annotations )
	{
		return this.rcd.cvsf.getConverter( type, annotations );
	}

	@Override
	public MediaType getContentType()
	{
		return this.consumes.stream().filter( m -> !m.isWildcardType() && !m.isWildcardSubtype() )
			.findFirst()
			.orElse( defaultContentType() );
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

	void produces( String[] value )
	{
		stream( value ).map( MediaType::valueOf ).forEach( this.produces::add );
	}

	void consumes( String[] value )
	{
		stream( value ).map( MediaType::valueOf ).forEach( this.consumes::add );
	}

	void cookie( Cookie cookie )
	{
		this.cookies.add( cookie );
	}

	void form( String name, String value )
	{
		Form form;

		try {
			if( this.entity == null ) {
				this.entity = form = new Form();
			}
			else {
				form = (Form) this.entity;
			}
		}
		catch( final ClassCastException e ) {
			// TODO review this, could it be caught in the initialisation stage?
			throw new IllegalStateException( "Trying to mix a non-form entity with a Form" );
		}

		form.param( name, value );
	}

	void entity( Object entity )
	{
		if( this.entity != null ) {
			// TODO review this, could it be caught in the initialisation stage?
			throw new IllegalStateException( "The request entity has been already set" );
		}

		this.entity = entity;
	}

	private MediaType defaultContentType()
	{
		final Object mt = ofNullable( this.configuration.getProperty( RestClientProperties.DEFAULT_CONTENT_TYPE ) )
			.map( o -> ( o instanceof MediaType ) ? o : trimToNull( Objects.toString( o, null ) ) )
			.orElse( this.entity instanceof Form ? MediaType.APPLICATION_FORM_URLENCODED_TYPE : MediaType.APPLICATION_OCTET_STREAM_TYPE );

		return mt instanceof MediaType ? (MediaType) mt : MediaType.valueOf( (String) mt );
	}
}
