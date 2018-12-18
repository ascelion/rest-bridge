
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.ws.rs.*;
import javax.ws.rs.client.WebTarget;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;

import io.leangen.geantyref.GenericTypeReflector;

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

	private final Class<?> type;
	private final Method javaMethod;
	private final Supplier<WebTarget> target;
	private final String httpMethod;
	private final List<Action> actions = new ArrayList<>( 8 );
	private final Type returnType;
	private final Map<String, Boolean> pathElements = new LinkedHashMap<>();

	RestMethod( ConvertersFactory cvsf, Class<?> type, Method method, Supplier<WebTarget> target )
	{
		this.type = type;
		this.javaMethod = method;
		this.httpMethod = Util.getHttpMethod( method );
		this.target = target;
		this.returnType = GenericTypeReflector.getExactReturnType( this.javaMethod, type );

		final String paths = Stream.of( method.getAnnotation( Path.class ), type.getAnnotation( Path.class ) )
			.filter( Objects::nonNull )
			.map( Path::value )
			.collect( joining() );

		Util.pathElements( paths ).forEach( p -> this.pathElements.put( p, false ) );

		final Parameter[] params = method.getParameters();

		final boolean hasEntity = false;

		this.actions.add( new ValidationAction( method ) );

		for( int q = 0, z = params.length; q < z; q++ ) {
			final int index = q;
			final Annotation[] annotations = params[index].getAnnotations();
			final Function<Object, String> cvt = cvsf.getConverter( params[index].getType(), annotations );
			final ActionParam p = new ActionParam( index, params[index].getType(), annotations, req -> req.arguments[index], cvt );
			final boolean entityCandidate = collectActions( annotations, p );

			if( entityCandidate ) {
				if( hasEntity ) {
					// TODO
					throw new RuntimeException( "already has entity" );
				}

				final Type entityType = GenericTypeReflector.getExactParameterTypes( method, type )[index];

				this.actions.add( new EntityAction( p, entityType ) );
			}
		}

		this.pathElements.entrySet().stream()
			.filter( e -> !e.getValue() )
			.findFirst().ifPresent( e -> {
				throw new RestClientMethodException( format( "Missing @PathParam for element %s", e.getKey() ), method );
			} );

		Util.findAnnotation( Produces.class, method, type )
			.ifPresent( a -> this.actions.add( new ProducesAction( a, this.actions.size() ) ) );
		Util.findAnnotation( Consumes.class, method, type )
			.ifPresent( a -> this.actions.add( new ConsumesAction( a, this.actions.size() ) ) );

		if( this.httpMethod == null ) {
//			resource methods that have a @Path annotation,
//			but no HTTP method are considered sub-resource locators.
			this.actions.add( new SubresourceAction( this.actions.size(), (Class) this.returnType, cvsf ) );
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

	private boolean collectActions( Annotation a, ActionParam p )
	{
		if( CookieParam.class.isInstance( a ) ) {
			this.actions.add( new CookieParamAction( (CookieParam) a, p ) );

			return false;
		}
		if( FormParam.class.isInstance( a ) ) {
			this.actions.add( new FormParamAction( (FormParam) a, p ) );

			return false;
		}
		if( MatrixParam.class.isInstance( a ) ) {
			this.actions.add( new MatrixParamAction( (MatrixParam) a, p ) );

			return false;
		}
		if( PathParam.class.isInstance( a ) ) {
			final PathParam v = (PathParam) a;

			if( this.pathElements.containsKey( v.value() ) ) {
				this.pathElements.put( v.value(), true );
			}
			else {
				throw new RestClientMethodException( format( "Unknown path element %s", v.value() ), this.javaMethod );
			}

			this.actions.add( new PathParamAction( v, p ) );

			return false;
		}
		if( QueryParam.class.isInstance( a ) ) {
			this.actions.add( new QueryParamAction( (QueryParam) a, p ) );

			return false;
		}
		if( HeaderParam.class.isInstance( a ) ) {
			this.actions.add( new HeaderParamAction( (HeaderParam) a, p ) );

			return false;
		}
		if( BeanParam.class.isInstance( a ) ) {
			final Iterable<Field> fields = getAllFieldsList( p.type );

			for( final Field field : fields ) {
				field.setAccessible( true );

				final Function<RestRequest, Object> sup = req -> {
					try {
						return field.get( p.currentValue( req ) );
					}
					catch( IllegalArgumentException | IllegalAccessException e ) {
						// TODO
						throw new RuntimeException( e );
					}
				};

				collectActions( field.getAnnotations(), new ActionParam( p.index, field.getType(), field.getAnnotations(), sup, p.converter ) );
			}

			return false;
		}

		return true;
	}

	Callable<?> request( Object proxy, Object... arguments ) throws URISyntaxException
	{
		final RestRequest req = new RestRequest( proxy, this.httpMethod, actualTarget(), this.returnType, arguments );
		Callable<?> res = null;

		for( final Action a : this.actions ) {
			res = a.execute( req );
		}

		return res;
	}

	private WebTarget actualTarget()
	{
		return Util.addPathFromAnnotation( this.javaMethod, this.target.get() );
	}
}
