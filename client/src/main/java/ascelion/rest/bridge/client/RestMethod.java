
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.*;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import static ascelion.rest.bridge.client.RBUtils.addPathFromAnnotation;
import static ascelion.rest.bridge.client.RBUtils.findAnnotation;
import static ascelion.rest.bridge.client.RBUtils.getHttpMethod;
import static ascelion.rest.bridge.client.RBUtils.pathElements;
import static io.leangen.geantyref.GenericTypeReflector.getExactParameterTypes;
import static io.leangen.geantyref.GenericTypeReflector.getExactReturnType;
import static java.lang.String.format;
import static java.security.AccessController.doPrivileged;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;

import lombok.SneakyThrows;

final class RestMethod
{

	static <T extends Annotation> T getAnnotation( Class<?> cls, Class<T> annCls )
	{
		T a = cls.getAnnotation( annCls );

		if( a != null ) {
			return a;
		}

		for( final Class<?> c : cls.getInterfaces() ) {
			a = getAnnotation( c, annCls );

			if( a != null ) {
				return a;
			}
		}

		return null;
	}

	//	static private final Logger L = LoggerFactory.getLogger( RestMethod.class );

	private final RestClientData rcd;
	private final Method javaMethod;
	private final GenericType<?> returnType;
	private boolean async;
	private final String httpMethod;
	private final List<Action> actions = new ArrayList<>( 8 );
	private final Map<String, Boolean> pathElements = new LinkedHashMap<>();

	RestMethod( RestClientData rcd, Method method )
	{
		this.rcd = rcd;
		this.javaMethod = method;
		this.httpMethod = getHttpMethod( method );

		final Type rt = getExactReturnType( this.javaMethod, rcd.type );
		GenericType<?> gt = new GenericType<>( rt );

		if( gt.getRawType() == CompletionStage.class ) {
			this.async = true;

			gt = new GenericType<>( ( (ParameterizedType) gt.getType() ).getActualTypeArguments()[0] );
		}
		else {
			this.async = false;

			gt = new GenericType<>( rt );
		}

		this.returnType = gt;

		final String paths = Stream.of( method.getAnnotation( Path.class ), rcd.type.getAnnotation( Path.class ) )
			.filter( Objects::nonNull )
			.map( Path::value )
			.collect( joining() );

		pathElements( paths ).forEach( p -> this.pathElements.put( p, false ) );

		final Parameter[] params = method.getParameters();

		this.actions.add( new ValidationAction( method ) );

		for( int q = 0, z = params.length; q < z; q++ ) {
			final int index = q;
			final Annotation[] annotations = params[index].getAnnotations();
			final Function<Object, String> cvt = (Function) rcd.cvsf.getConverter( params[index].getType(), annotations );
			final ActionParam p = new ActionParam( index, params[index].getType(), annotations, req -> req.rc.getArgumentAt( index ), cvt );

			if( collectActions( annotations, p ) ) {
				this.actions.stream()
					.filter( EntityAction.class::isInstance )
					.forEach( a -> {
						throw new RestClientMethodException( "An entity is already present at parameter " + a.param.index, this.javaMethod );
					} );

				final Type entityType = getExactParameterTypes( this.javaMethod, this.rcd.type )[index];
				final EntityAction entityAction = new EntityAction( p, entityType );

				this.actions.add( entityAction );
			}
		}

		findAnnotation( Produces.class, method, rcd.type )
			.ifPresent( a -> this.actions.add( new ProducesAction( a, this.actions.size() ) ) );
		findAnnotation( Consumes.class, method, rcd.type )
			.ifPresent( a -> this.actions.add( new ConsumesAction( a, this.actions.size() ) ) );

		this.pathElements.entrySet().stream()
			.filter( e -> !e.getValue() )
			.findFirst().ifPresent( e -> {
				throw new RestClientMethodException( format( "Missing @PathParam for element %s", e.getKey() ), method );
			} );

		if( this.httpMethod == null ) {
//			resource methods that have a @Path annotation,
//			but no HTTP method are considered sub-resource locators.
			this.actions.add( new SubresourceAction( this.actions.size(), this.javaMethod.getReturnType(), rcd ) );
		}

		Collections.sort( this.actions );
	}

	private boolean collectActions( Annotation[] annotations, ActionParam p )
	{
		boolean entityCandidate = true;

		for( final Annotation a : annotations ) {
			if( !collectActions( a, p ) ) {
				entityCandidate = false;
			}
		}

		return entityCandidate;
	}

	private boolean collectActions( Annotation annot, ActionParam param )
	{
		if( CookieParam.class.isInstance( annot ) ) {
			this.actions.add( new CookieParamAction( (CookieParam) annot, param ) );

			return false;
		}
		if( FormParam.class.isInstance( annot ) ) {
			this.actions.add( new FormParamAction( (FormParam) annot, param ) );

			return false;
		}
		if( MatrixParam.class.isInstance( annot ) ) {
			this.actions.add( new MatrixParamAction( (MatrixParam) annot, param ) );

			return false;
		}
		if( PathParam.class.isInstance( annot ) ) {
			final PathParam v = (PathParam) annot;

			if( this.pathElements.containsKey( v.value() ) ) {
				this.pathElements.put( v.value(), true );
			}
			else {
				throw new RestClientMethodException( format( "Unknown path element %s", v.value() ), this.javaMethod );
			}

			this.actions.add( new PathParamAction( v, param ) );

			return false;
		}
		if( QueryParam.class.isInstance( annot ) ) {
			this.actions.add( new QueryParamAction( (QueryParam) annot, param ) );

			return false;
		}
		if( HeaderParam.class.isInstance( annot ) ) {
			this.actions.add( new HeaderParamAction( (HeaderParam) annot, param ) );

			return false;
		}
		if( BeanParam.class.isInstance( annot ) ) {
			final Iterable<Field> fields = doPrivileged( (PrivilegedAction<Iterable<Field>>) () -> getAllFieldsList( param.type ) );
			boolean entityCandidate = true;

			for( final Field field : fields ) {
				final Function<RestRequest<?>, Object> sup = req -> readFieldValue( req, param, field );

				if( !collectActions( field.getAnnotations(), new ActionParam( param.index, field.getType(), field.getAnnotations(), sup, param.converter ) ) ) {
					entityCandidate = false;
				}
			}

			return entityCandidate;
		}

		return true;
	}

	Callable<?> request( Object proxy, Object... arguments ) throws URISyntaxException
	{
		final WebTarget actualTarget = addPathFromAnnotation( this.javaMethod, this.rcd.tsup.get() );
		final RestRequestContextImpl rc = new RestRequestContextImpl( actualTarget, this.rcd.type, this.javaMethod, proxy, arguments );
		final RestRequest<?> req = new RestRequest<>( this.rcd, proxy, this.returnType, this.async, this.httpMethod, rc );
		Callable<?> res = null;

		for( final Action a : this.actions ) {
			res = a.execute( req );
		}

		return res;
	}

	@SneakyThrows
	private Object readFieldValue( RestRequest<?> req, ActionParam param, Field field )
	{
		try {
			return doPrivileged( (PrivilegedExceptionAction<?>) () -> readField( field, param.currentValue( req ), true ) );
		}
		catch( final PrivilegedActionException e ) {
			throw e.getCause();
		}
	}
}
