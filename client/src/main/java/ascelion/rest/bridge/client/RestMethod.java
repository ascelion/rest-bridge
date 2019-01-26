
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ascelion.utils.chain.InterceptorChain;

import static ascelion.rest.bridge.client.RBUtils.findAnnotation;
import static ascelion.rest.bridge.client.RBUtils.getHttpMethod;
import static ascelion.rest.bridge.client.RBUtils.pathParameters;
import static java.lang.String.format;
import static java.security.AccessController.doPrivileged;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;

import lombok.SneakyThrows;

final class RestMethod
{

	private final RestClientData rcd;
	private final Method javaMethod;
	private final String httpMethod;
	private final GenericType<Object> returnType;
	private final boolean async;

	private final InterceptorChain<RestRequestContext> chain = new InterceptorChain<>();

	private final Map<String, Boolean> missingPaths;
	private final String methodURI;
	private RestParam entityParam;

	RestMethod( RestClientData rcd, Method method )
	{
		this.rcd = rcd;
		this.javaMethod = method;
		this.httpMethod = getHttpMethod( this.javaMethod );
		this.returnType = RBUtils.genericType( this.rcd.type, this.javaMethod );
		this.async = CompletionStage.class.equals( this.javaMethod.getReturnType() );

		this.methodURI = Stream.of( this.rcd.type.getAnnotation( Path.class ), this.javaMethod.getAnnotation( Path.class ) )
			.filter( Objects::nonNull )
			.map( Path::value )
			.map( RBUtils::trimSlashes )
			.collect( joining( "/", "/", "" ) );

		this.missingPaths = pathParameters( this.methodURI )
			.stream().collect( toMap( x -> x, x -> true ) );

		rcd.riFactories.forEach( f -> stream( f.create( rcd.type, this.javaMethod ) )
			.forEach( this.chain::add ) );

		this.chain.add( new INTRequestValidation() );

		ofNullable( this.javaMethod.getAnnotation( Valid.class ) )
			.ifPresent( a -> this.chain.add( new INTResponseValidation() ) );
		findAnnotation( Produces.class, this.javaMethod, this.rcd.type )
			.ifPresent( a -> this.chain.add( new INTProduces( a ) ) );
		findAnnotation( Consumes.class, this.javaMethod, this.rcd.type )
			.ifPresent( a -> this.chain.add( new INTConsumes( a ) ) );

		final Parameter[] params = this.javaMethod.getParameters();

		for( int q = 0, z = params.length; q < z; q++ ) {
			final int index = q;
			final Annotation[] annotations = params[index].getAnnotations();
			final LazyParamConverter<?> cvt = rcd.cvsf.getConverter( params[index].getType(), annotations );
			final RestParam p = new RestParam( index, params[index].getType(), cvt, params[index].getAnnotation( DefaultValue.class ), rc -> rc.getArgumentAt( index ) );

			if( createInterceptors( annotations, p ) ) {
				if( this.entityParam != null ) {
					throw new RestClientMethodException( "An entity is already present at parameter " + this.entityParam.index, this.javaMethod );
				}

				this.entityParam = p;

				this.chain.add( new INTEntity( p ) );
			}
		}

		final String missing = this.missingPaths.entrySet().stream()
			.filter( Map.Entry::getValue )
			.map( Map.Entry::getKey )
			.collect( joining( "}, {" ) );

		if( missing.length() > 0 ) {
			throw new RestClientMethodException( format( "Missing @PathParam for {%s}", missing ), this.javaMethod );
		}

		if( this.httpMethod == null ) {
//			resource methods that have a @Path annotation,
//			but no HTTP method are considered sub-resource locators.
			this.chain.add( new INTSubResource( this.rcd, this.returnType.getRawType() ) );
		}
		else {
			if( this.async ) {
				this.chain.add( new INTAsync() );
			}
		}
	}

	Object request( Object proxy, Object... arguments ) throws Exception
	{
		WebTarget target = this.rcd.tsup.get();

		target = target.path( this.methodURI );

		final RestRequestContext rc = new RestRequestContext( this.rcd, this.javaMethod, this.returnType, this.async, this.httpMethod, target, proxy, arguments );

		return this.chain.around( rc, () -> invoke( rc ) );
	}

	private boolean createInterceptors( Annotation[] annotations, RestParam p )
	{
		boolean entityCandidate = true;

		for( final Annotation a : annotations ) {
			if( !createInterceptors( a, p ) ) {
				entityCandidate = false;
			}
		}

		return entityCandidate;
	}

	private boolean createInterceptors( Annotation annotation, RestParam param )
	{
		if( CookieParam.class.isInstance( annotation ) ) {
			this.chain.add( new INTCookieParam( (CookieParam) annotation, param ) );

			return false;
		}
		if( FormParam.class.isInstance( annotation ) ) {
			this.chain.add( new INTFormParam( (FormParam) annotation, param ) );

			return false;
		}
		if( MatrixParam.class.isInstance( annotation ) ) {
			this.chain.add( new INTMatrixParam( (MatrixParam) annotation, param ) );

			return false;
		}
		if( PathParam.class.isInstance( annotation ) ) {
			final PathParam v = (PathParam) annotation;

			if( this.missingPaths.containsKey( v.value() ) ) {
				this.missingPaths.put( v.value(), false );
			}
			else {
				throw new RestClientMethodException( format( "Unknown path element %s", v.value() ), this.javaMethod );
			}

			this.chain.add( new INTPathParam( v, param ) );

			return false;
		}
		if( QueryParam.class.isInstance( annotation ) ) {
			this.chain.add( new INTQueryParam( (QueryParam) annotation, param ) );

			return false;
		}
		if( HeaderParam.class.isInstance( annotation ) ) {
			this.chain.add( new INTHeaderParam( (HeaderParam) annotation, param ) );

			return false;
		}
		if( BeanParam.class.isInstance( annotation ) ) {
			final Iterable<Field> fields = doPrivileged( (PrivilegedAction<Iterable<Field>>) () -> getAllFieldsList( param.type ) );
			boolean entityCandidate = true;

			for( final Field field : fields ) {
				final Function<RestRequestContext, Object> arg = rc -> readFieldValue( rc, param, field );
				final LazyParamConverter<?> cvt = this.rcd.cvsf.getConverter( field.getType(), field.getAnnotations() );
				final RestParam par = new RestParam( param.index, field.getType(), cvt, field.getAnnotation( DefaultValue.class ), arg );

				if( !createInterceptors( field.getAnnotations(), par ) ) {
					entityCandidate = false;
				}
			}

			return entityCandidate;
		}

		return true;
	}

	@SneakyThrows
	private Object readFieldValue( RestRequestContext rc, RestParam param, Field field )
	{
		try {
			return doPrivileged( (PrivilegedExceptionAction<?>) () -> readField( field, param.argument.apply( rc ), true ) );
		}
		catch( final PrivilegedActionException e ) {
			throw e.getCause();
		}
	}

	private Object invoke( RestRequestContext rc ) throws Exception
	{
		RestClient.invokedMethod( rc.getJavaMethod() );

		final Invocation.Builder b = rc.getTarget().request();

		rc.getHeaders().forEach( ( k, v ) -> {
			// Jersey concatenates header values while Resteasy sends multiple headers
			// On the other hand, use of Wiremock in MP-TCK expects concatenated values
			// (or it is a wiremock issue)
			b.header( k, v.stream().collect( joining( "," ) ) );
		} );
		rc.getCookies().forEach( b::cookie );

		b.accept( rc.produces.toArray( new MediaType[0] ) );

		final Response rsp;

		try {
			final MediaType ct = rc.getContentType();

			if( rc.entity != null ) {
				final Entity<?> e = Entity.entity( rc.entity, ct );

				rsp = b.method( rc.getHttpMethod(), e );
			}
			else {
				if( ct != null ) {
					// Micro TCK uses a GET with a Content-Type, so let's keep it happy!
					b.header( HttpHeaders.CONTENT_TYPE, ct );
				}

				rsp = b.method( rc.getHttpMethod() );
			}
		}
		catch( final ProcessingException e ) {
			final Throwable c = e.getCause();

			if( c instanceof RuntimeException ) {
				throw(RuntimeException) c;
			}
			else {
				throw e;
			}
		}
		finally {
			RestClient.invokedMethod( null );
		}

		final Throwable ex = rc.rcd.rsph.apply( rsp );

		if( ex != null ) {
			ex.fillInStackTrace();

			if( ex instanceof Error ) {
				throw(Error) ex;
			}

			throw(Exception) ex;
		}

		final Class<?> rawType = rc.getReturnType().getRawType();

		if( rawType == Response.class ) {
			return rsp;
		}

		try {
			if( rawType == void.class || rawType == Void.class ) {
				return null;
			}
			if( rsp.getStatus() == NO_CONTENT.getStatusCode() ) {
				return null;
			}
			else {
				return rsp.readEntity( rc.getReturnType() );
			}
		}
		finally {
			rsp.close();
		}
	}

}
