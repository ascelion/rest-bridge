
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;

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

	static boolean isValidation( Annotation a )
	{
		return "javax.validation.Valid".equals( a.annotationType().getName() );
	}

	//	static private final Logger L = LoggerFactory.getLogger( RestMethod.class );

	private final Class<?> cls;

	final Method method;

	WebTarget target;

	private final Class<?> returnType;

	private final String httpMethod;

	private final Collection<Action> actions = new TreeSet<>();

	RestMethod( Class<?> cls, Method method, WebTarget target )
	{
		this.cls = cls;
		this.method = method;
		this.returnType = method.getReturnType();
		this.httpMethod = Util.getHttpMethod( method );
		this.target = Util.addPathFromAnnotation( method, target );

		final Type exactReturnType = GenericTypeReflector.getExactReturnType( this.method, this.cls );

		if( this.httpMethod == null ) {
			if( this.target == target ) {
				throw new UnsupportedOperationException( "Not a resource method: " + method );
			}

			this.actions.add( new SubresourceAction( (Class) exactReturnType ) );
		}
		else {
			final Class<?>[] types = method.getParameterTypes();

			for( int k = 0, z = types.length; k < z; k++ ) {
				this.actions.add( new SetValueAction( k ) );

				final Annotation[] pas = method.getParameterAnnotations()[k];
				boolean entityCandidate = true;

				for( final Annotation a : pas ) {
					final Action action = Action.createAction( a, k );

					if( action != null ) {
						this.actions.add( action );

						// TODO: fix this dirty hack
						if( !"ValidationAction".equals( action.getClass().getSimpleName() ) ) {
							entityCandidate = false;
						}
					}
				}

				if( entityCandidate ) {
					final Type entityType = GenericTypeReflector.getExactParameterTypes( method, cls )[k];

					this.actions.add( new EntityAction( entityType, k ) );
				}
			}

			this.actions.add( new ValidationAction( this.actions.size() ) );

			final Produces produces = getAnnotation( Produces.class );

			if( produces != null ) {
				this.actions.add( new ProducesAction( produces, this.actions.size() ) );
			}

			final Consumes consumes = getAnnotation( Consumes.class );

			if( consumes != null ) {
				this.actions.add( new ConsumesAction( consumes, this.actions.size() ) );
			}

			this.actions.add( new InvokeAction( this.actions.size(), this.httpMethod, exactReturnType ) );
		}
	}

	void call( RestContext context )
	{
		for( final Action a : this.actions ) {
			a.evaluate( context.arguments );
			a.execute( context );
		}
		;
	}

	private <T extends Annotation> T getAnnotation( Class<T> annCls )
	{
		final T annot = this.method.getAnnotation( annCls );

		if( annot != null ) {
			return annot;
		}

		return getAnnotation( this.cls, annCls );
	}
}
