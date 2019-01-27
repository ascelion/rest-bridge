
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ParamConverter;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public /*final*/ class RestRequestContext extends RestMethodInfo
{

	@Getter
	@Setter
	private WebTarget reqTarget;

	@Getter( value = AccessLevel.NONE )
	final Object proxy;
	private final List<Object> arguments;
	@Getter
	private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
	@Getter
	private final Collection<Cookie> cookies = new ArrayList<>();
	final Collection<MediaType> produces = new ArrayList<>();
	private final Collection<MediaType> consumes = new ArrayList<>();
	Object entity;

	RestRequestContext( RestMethodInfo rmi, Object proxy, Object[] arguments )
	{
		super( rmi );

		this.reqTarget = getTarget().get().path( rmi.getMethodURI() );
		this.proxy = proxy;
		this.arguments = arguments != null ? asList( arguments ) : emptyList();
	}

	public Object getImplementation()
	{
		return this.proxy;
	}

	public Object getArgumentAt( int index )
	{
		return this.arguments.get( index );
	}

	public <T> T getArgumentAt( Class<T> type, int index )
	{
		return type.cast( this.arguments.get( index ) );
	}

	public Object[] getArguments()
	{
		return this.arguments.toArray();
	}

	public <T> ParamConverter<T> getConverter( Class<T> type, Annotation[] annotations )
	{
		return getConvertersFactory().getConverter( type, annotations );
	}

	public MediaType getContentType()
	{
		return this.consumes.stream().filter( m -> !m.isWildcardType() && !m.isWildcardSubtype() )
			.findFirst()
			.orElse( defaultContentType() );
	}

	void header( String name, String value )
	{
		if( value != null ) {
			this.headers.add( name, value );
		}
		else {
			this.headers.remove( name );
		}
	}

	void matrix( String name, String value )
	{
		this.reqTarget = this.reqTarget.matrixParam( name, value );
	}

	void path( String name, String value )
	{
		this.reqTarget = this.reqTarget.resolveTemplate( name, value, true );
	}

	void query( String name, String value )
	{
		this.reqTarget = this.reqTarget.queryParam( name, value );
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
		final Object mt = ofNullable( getConfiguration().getProperty( RestClientProperties.DEFAULT_CONTENT_TYPE ) )
			.map( o -> ( o instanceof MediaType ) ? o : trimToNull( Objects.toString( o, null ) ) )
			.orElse( this.entity instanceof Form ? MediaType.APPLICATION_FORM_URLENCODED_TYPE : MediaType.APPLICATION_OCTET_STREAM_TYPE );

		return mt instanceof MediaType ? (MediaType) mt : MediaType.valueOf( (String) mt );
	}
}
