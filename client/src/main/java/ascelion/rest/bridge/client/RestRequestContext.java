
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
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

import lombok.Getter;
import lombok.Setter;

public /*final*/ class RestRequestContext
{

	static final ThreadLocal<RestRequestContext> TL = new ThreadLocal<RestRequestContext>()
	{

		@Override
		protected RestRequestContext initialValue()
		{
			throw new IllegalStateException( "No RestRequestContext bound to the current thread" );
		};
	};

	static public RestRequestContext getCurrentRequestContext()
	{
		return TL.get();
	}

	static public Method getJavaMethod()
	{
		return TL.get().getMethodInfo().getJavaMethod();
	}

	@Getter
	@Setter
	private WebTarget reqTarget;

	@Getter
	private final Object service;
	private final List<Object> arguments;
	@Getter
	private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
	@Getter
	private final Collection<Cookie> cookies = new ArrayList<>();
	final Collection<MediaType> produces = new ArrayList<>();
	private final Collection<MediaType> consumes = new ArrayList<>();
	private Object entity;
	@Getter
	private final RestMethodInfo methodInfo;
	private boolean hasBody;

	RestRequestContext( RestMethodInfo rmi, Object service, Object[] arguments )
	{
		this.methodInfo = rmi;
		this.reqTarget = this.methodInfo.getTarget().get().path( rmi.getMethodURI() );
		this.service = service;
		this.arguments = arguments != null ? asList( arguments ) : emptyList();
	}

	@Override
	public String toString()
	{
		return this.methodInfo.toString();
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
		return this.methodInfo.getConvertersFactory().getConverter( type, annotations );
	}

	public Configuration getConfiguration()
	{
		return this.methodInfo.getConfiguration();
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

		this.hasBody = true;
	}

	void entity( Object entity )
	{
		if( this.entity != null ) {
			// TODO review this, could it be caught in the initialisation stage?
			throw new IllegalStateException( "The request entity has been already set" );
		}

		this.entity = entity;

		this.hasBody = true;
	}

	Object entity()
	{
		return this.entity;
	}

	boolean hasBody()
	{
		return this.hasBody;
	}

	private MediaType defaultContentType()
	{
		final Configuration cf = this.methodInfo.getConfiguration();
		final Object mt = ofNullable( cf.getProperty( RestClientProperties.DEFAULT_CONTENT_TYPE ) )
			.map( o -> ( o instanceof MediaType ) ? o : trimToNull( Objects.toString( o, null ) ) )
			.orElse( this.entity instanceof Form ? MediaType.APPLICATION_FORM_URLENCODED_TYPE : MediaType.APPLICATION_OCTET_STREAM_TYPE );

		return mt instanceof MediaType ? (MediaType) mt : MediaType.valueOf( (String) mt );
	}
}
