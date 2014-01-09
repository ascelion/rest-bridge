
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.googlecode.gentyref.GenericTypeReflector;

class RestMethod
{

	interface Action
	{

		void evaluate( RestContext cx );

		void prepare( Object[] arguments );
	}

	static abstract class AnnotationAction<A extends Annotation>
	implements Action, Comparable<AnnotationAction<Annotation>>
	{

		enum Priority
		{
			SET_VALUE,
			DEFAULT_VALUE,
			VALID_VALUE,
			ANY,
		}

		final A annotation;

		final int ix;

		private final Priority px;

		AnnotationAction( A annotation, int ix )
		{
			this( annotation, ix, Priority.ANY );
		}

		AnnotationAction( A annotation, int ix, Priority px )
		{
			this.annotation = annotation;
			this.ix = ix;
			this.px = px;
		}

		@Override
		public int compareTo( AnnotationAction<Annotation> o )
		{
			if( this.ix != o.ix ) {
				return this.ix - o.ix;
			}

			final int cp = this.px.compareTo( o.px );

			if( cp != 0 ) {
				return cp;
			}

			if( this.annotation == null ) {
				return o.annotation == null ? 0 : +1;
			}
			if( o.annotation == null ) {
				return -1;
			}

			return this.annotation.getClass().getName().compareTo( o.annotation.getClass().getSimpleName() );
		}

		@Override
		public void prepare( Object[] unused )
		{
		}

		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder();

			builder.append( getClass().getSimpleName() );
			builder.append( "[ix=" );
			builder.append( this.ix );
			builder.append( ", px=" );
			builder.append( this.px );
			builder.append( ", annotation=" );
			builder.append( this.annotation );
			builder.append( "]" );

			return builder.toString();
		}

		Collection collection( RestContext cx, Consumer action )
		{
			final Collection c;

			if( cx.value instanceof Collection ) {
				c = (Collection) cx.value;
			}
			else if( cx.value instanceof Object[] ) {
				c = Arrays.asList( (Object[]) cx.value );
			}
			else if( cx.value != null ) {
				c = Arrays.asList( cx.value );
			}
			else {
				c = Collections.EMPTY_LIST;
			}

			if( action != null ) {
				c.forEach( action );
			}

			return c;
		}
	}

	static class BeanParamAction
	extends AnnotationAction<BeanParam>
	{

		BeanParamAction( BeanParam a, int ix )
		{
			super( a, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			if( cx.value != null ) {
				final Object bean = cx.value;

				Util.getDeclaredFields( cx.value.getClass() ).forEach( f -> addAction( cx, f, bean ) );
			}
		}

		private void addAction( RestContext cx, Field field, Object bean )
		{
			final Collection<Action> actions = new LinkedList<>();

			for( final Annotation a : field.getAnnotations() ) {
				final Action action = createAction( a, 0 );

				if( action != null ) {
					actions.add( action );
				}
			}

			if( actions.size() > 0 ) {
				try {
					cx.value = field.get( bean );
				}
				catch( final IllegalAccessException e ) {
					throw new RuntimeException( e );
				}

				actions.forEach( a -> a.evaluate( cx ) );
			}
		}
	}

	static class ConsumesAction
	extends AnnotationAction<Consumes>
	{

		ConsumesAction( Consumes annotation )
		{
			super( annotation, Integer.MAX_VALUE - 2 );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			if( this.annotation.value().length > 0 ) {
				cx.contentType = this.annotation.value()[0];
			}
		}
	}

	static class CookieParamAction
	extends AnnotationAction<CookieParam>
	{

		CookieParamAction( CookieParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			collection( cx, v -> {
				if( v instanceof Cookie ) {
					final Cookie c = (Cookie) v;

					cx.cookies.add( new Cookie( this.annotation.value(),
						c.getValue(), c.getPath(), c.getDomain(), c.getVersion() ) );
				}
				else {
					cx.cookies.add( new Cookie( this.annotation.value(), v.toString() ) );
				}
			} );
		}

	}

	static class DefaultValueAction
	extends AnnotationAction<DefaultValue>
	{

		public DefaultValueAction( DefaultValue annotation, int ix )
		{
			super( annotation, ix, Priority.DEFAULT_VALUE );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			if( cx.value == null ) {
				cx.value = this.annotation.value();
			}
		}

	}

	static class EntityAction
	extends AnnotationAction<Annotation>
	{

		final Type entityType;

		EntityAction( Type entityType, int ix )
		{
			super( null, ix );

			this.entityType = entityType;
		}

		@Override
		public void evaluate( RestContext cx )
		{
			cx.entity = cx.value;
		}
	}

	static class FormParamAction
	extends AnnotationAction<FormParam>
	{

		FormParamAction( FormParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			collection( cx, v -> cx.form.param( this.annotation.value(), v.toString() ) );
		}

	}

	static class HeaderParamAction
	extends AnnotationAction<HeaderParam>
	{

		HeaderParamAction( HeaderParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			collection( cx, v -> cx.headers.add( this.annotation.value(), v ) );
		}

	}

	static class MatrixParamAction
	extends AnnotationAction<MatrixParam>
	{

		MatrixParamAction( MatrixParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
		}

	}

	static class PathParamAction
	extends AnnotationAction<PathParam>
	{

		PathParamAction( PathParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			cx.target = cx.target.resolveTemplate( this.annotation.value(), cx.value );
		}

	}

	static class ProducesAction
	extends AnnotationAction<Produces>
	{

		ProducesAction( Produces annotation )
		{
			super( annotation, Integer.MAX_VALUE - 1 );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			cx.accepts = this.annotation.value();
		}

	}

	static class QueryParamAction
	extends AnnotationAction<QueryParam>
	{

		QueryParamAction( QueryParam annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			cx.target = cx.target.queryParam( this.annotation.value(), collection( cx, null ).toArray() );
		}
	}

	class SetValueAction
	extends AnnotationAction<Annotation>
	{

		private Object value;

		SetValueAction( int ix )
		{
			super( null, ix, Priority.SET_VALUE );
		}

		@Override
		public void evaluate( RestContext cx )
		{
			cx.value = this.value;
		}

		@Override
		public void prepare( Object[] arguments )
		{
			this.value = arguments[this.ix];
		}
	}

	static class ValidationAction
	extends AnnotationAction<Annotation>
	{

		ValidationAction( Annotation annotation, int ix )
		{
			super( annotation, ix );
		}

		@Override
		public void evaluate( RestContext cx )
		{
		}
	}

	static Action createAction( Annotation a, int ix )
	{
		if( CookieParam.class.isInstance( a ) ) {
			return new CookieParamAction( (CookieParam) a, ix );
		}
		if( DefaultValue.class.isInstance( a ) ) {
			return new DefaultValueAction( (DefaultValue) a, ix );
		}
		if( FormParam.class.isInstance( a ) ) {
			return new FormParamAction( (FormParam) a, ix );
		}
		if( MatrixParam.class.isInstance( a ) ) {
			return new MatrixParamAction( (MatrixParam) a, ix );
		}
		if( PathParam.class.isInstance( a ) ) {
			return new PathParamAction( (PathParam) a, ix );
		}
		if( QueryParam.class.isInstance( a ) ) {
			return new QueryParamAction( (QueryParam) a, ix );
		}
		if( HeaderParam.class.isInstance( a ) ) {
			return new HeaderParamAction( (HeaderParam) a, ix );
		}
		if( BeanParam.class.isInstance( a ) ) {
			return new BeanParamAction( (BeanParam) a, ix );
		}

		return null;
	}

	static <T extends Annotation> T getAnnotation( Class cls, Class<T> annCls )
	{
		T a = (T) cls.getAnnotation( annCls );

		if( a != null ) {
			return a;
		}

		for( final Class c : cls.getInterfaces() ) {
			a = getAnnotation( c, annCls );

			if( a != null ) {
				return a;
			}
		}

		return null;
	}

	//	static private final Logger L = LoggerFactory.getLogger( RestMethod.class );

	private final Class cls;

	private final Method method;

	private final Class returnType;

	private final String httpMethod;

	private final WebTarget target;

	private final Collection<Action> actions = new TreeSet<>();

	RestMethod( Class cls, Method method, WebTarget target )
	{
		this.cls = cls;
		this.method = method;
		this.returnType = method.getReturnType();
		this.httpMethod = Util.getHttpMethod( method );
		this.target = Util.addPathFromAnnotation( method, target );

		if( this.httpMethod == null ) {
			if( this.target == target ) {
				throw new UnsupportedOperationException( "Not a resource method." );
			}
			else if( !this.returnType.isInterface() ) {
				throw new UnsupportedOperationException( "Return type not an interface" );
			}
		}

		final Class<?>[] types = method.getParameterTypes();

		for( int k = 0, z = types.length; k < z; k++ ) {
			this.actions.add( new SetValueAction( k ) );

			final Annotation[] pas = method.getParameterAnnotations()[k];
			boolean entityCandidate = true;

			for( final Annotation a : pas ) {
				final Action action = createAction( a, k );

				if( action != null ) {
					this.actions.add( action );

					entityCandidate = false;
				}
			}

			if( entityCandidate ) {
				final Type entityType = GenericTypeReflector.getExactParameterTypes( method, cls )[k];

				this.actions.add( new EntityAction( entityType, k ) );
			}
		}

		final Produces produces = getAnnotation( Produces.class );

		if( produces != null ) {
			this.actions.add( new ProducesAction( produces ) );
		}

		final Consumes consumes = getAnnotation( Consumes.class );
		if( consumes != null ) {
			this.actions.add( new ConsumesAction( consumes ) );
		}
	}

	Object call( Object[] arguments, MultivaluedMap<String, Object> headers, Collection<Cookie> cookies, Form form )
	{
		final Type exactReturnType = GenericTypeReflector.getExactReturnType( this.method, this.cls );

		if( this.httpMethod == null ) {
			final Class exactClass = (Class) exactReturnType;
			final RestClientIH ih = new RestClientIH( exactClass, this.target, headers, cookies, form );

			return RestClientIH.newProxy( exactClass, ih );
		}

		final RestContext cx = new RestContext( this.target, headers, cookies, form );

		this.actions.forEach( a -> {
			a.prepare( arguments );
			a.evaluate( cx );
		} );

		final Invocation.Builder b;

		if( cx.accepts != null ) {
			b = cx.target.request( cx.accepts );
		}
		else {
			b = cx.target.request();
		}

		b.headers( cx.headers );

		cx.cookies.forEach( c -> b.cookie( c ) );

		if( cx.entity == null && !cx.form.asMap().isEmpty() ) {
			cx.entity = form;
			cx.contentType = MediaType.APPLICATION_FORM_URLENCODED;
		}
		else {
			if( cx.contentType == null ) {
				cx.contentType = MediaType.APPLICATION_OCTET_STREAM;
			}
			if( !cx.form.asMap().isEmpty() ) {
				if( cx.entity instanceof Form ) {
					( (Form) cx.entity ).asMap().putAll( form.asMap() );
				}
				else {
					// TODO
					throw new BadRequestException();
				}
			}
		}

		final GenericType genericReturnType = new GenericType( exactReturnType );

		if( cx.entity != null ) {
			return b.method( this.httpMethod, Entity.entity( cx.entity, cx.contentType ), genericReturnType );
		}
		else {
			return b.method( this.httpMethod, genericReturnType );
		}
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
