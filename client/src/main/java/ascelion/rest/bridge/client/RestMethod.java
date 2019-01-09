
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.*;
import javax.ws.rs.client.WebTarget;

import static java.lang.String.format;
import static java.security.AccessController.doPrivileged;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;

import io.leangen.geantyref.GenericTypeReflector;
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

	private final RestBridgeType rbt;
	private final Method javaMethod;
	private final String httpMethod;
	private final List<Action> actions = new ArrayList<>( 8 );
	private final Type returnType;
	private final Map<String, Boolean> pathElements = new LinkedHashMap<>();

	RestMethod( RestBridgeType rbt, Method method )
	{
		this.rbt = rbt;
		this.javaMethod = method;
		this.httpMethod = Util.getHttpMethod( method );
		this.returnType = GenericTypeReflector.getExactReturnType( this.javaMethod, rbt.type );

		final String paths = Stream.of( method.getAnnotation( Path.class ), rbt.type.getAnnotation( Path.class ) )
			.filter( Objects::nonNull )
			.map( Path::value )
			.collect( joining() );

		Util.pathElements( paths ).forEach( p -> this.pathElements.put( p, false ) );

		final Parameter[] params = method.getParameters();

		this.actions.add( new ValidationAction( method ) );

		for( int q = 0, z = params.length; q < z; q++ ) {
			final int index = q;
			final Annotation[] annotations = params[index].getAnnotations();
			final Function<Object, String> cvt = (Function) rbt.cvsf.getConverter( params[index].getType(), annotations );
			final ActionParam p = new ActionParam( index, params[index].getType(), annotations, req -> req.arguments[index], cvt );

			if( collectActions( annotations, p ) ) {
				this.actions.stream()
					.filter( EntityAction.class::isInstance )
					.forEach( a -> {
						throw new RestClientMethodException( "An entity is already present at parameter " + a.param.index, this.javaMethod );
					} );

				final Type entityType = GenericTypeReflector.getExactParameterTypes( this.javaMethod, this.rbt.type )[index];
				final EntityAction entityAction = new EntityAction( p, entityType );

				this.actions.add( entityAction );
			}
		}

		Util.findAnnotation( Produces.class, method, rbt.type )
			.ifPresent( a -> this.actions.add( new ProducesAction( a, this.actions.size() ) ) );
		Util.findAnnotation( Consumes.class, method, rbt.type )
			.ifPresent( a -> this.actions.add( new ConsumesAction( a, this.actions.size() ) ) );

		this.pathElements.entrySet().stream()
			.filter( e -> !e.getValue() )
			.findFirst().ifPresent( e -> {
				throw new RestClientMethodException( format( "Missing @PathParam for element %s", e.getKey() ), method );
			} );

		if( this.httpMethod == null ) {
//			resource methods that have a @Path annotation,
//			but no HTTP method are considered sub-resource locators.
			this.actions.add( new SubresourceAction( this.actions.size(), (Class) this.returnType, rbt ) );
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
				final Function<RestRequest, Object> sup = req -> readFieldValue( req, param, field );

				if( !collectActions( field.getAnnotations(), new ActionParam( param.index, field.getType(), field.getAnnotations(), sup, param.converter ) ) ) {
					entityCandidate = false;
				}
			}

			return entityCandidate;
		}

		return true;
	}

	@SneakyThrows
	private Object readFieldValue( RestRequest request, ActionParam param, Field field )
	{
		try {
			return doPrivileged( (PrivilegedExceptionAction) () -> readField( field, param.currentValue( request ), true ) );
		}
		catch( final PrivilegedActionException e ) {
			throw e.getCause();
		}
	}

	Callable<?> request( Object proxy, Object... arguments ) throws URISyntaxException
	{
		final WebTarget actualTarget = Util.addPathFromAnnotation( this.javaMethod, this.rbt.tsup.get() );
		final RestRequest req = new RestRequest( this.rbt, proxy, this.httpMethod, actualTarget, this.returnType, arguments );
		Callable<?> res = null;

		for( final Action a : this.actions ) {
			res = a.execute( req );
		}

		return res;
	}
}
