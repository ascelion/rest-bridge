
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class AnnotationActionTest
{

	private static final String ANNOTATION_VALUE = "name";
	private static final String PARAM_VALUE = "value";

	private final MockClient mc = new MockClient();
	private RestMethod met;
	private List<Action> actions;

	@Before
	public void setUp() throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		final ConvertersFactory cvsf = new ConvertersFactory( this.mc.configuration );

		final Method m = Interface.class.getMethod( "get" );
		this.met = new RestMethod( cvsf, Interface.class, m, () -> this.mc.methodTarget );
		this.actions = (List<Action>) readDeclaredField( this.met, "actions", true );

		this.actions.clear();
	}

	@Test
	public void consumes() throws Exception
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, new String[] { "type/subtype" } );
		final Consumes ann = TypeFactory.annotation( Consumes.class, map );

		createAction( FormParam.class, FormParamAction::new );
		addAction( new ConsumesAction( ann, -1 ) );

		final Object[] arguments = new Object[3];

		when( this.mc.bld.method( any( String.class ), any( Entity.class ), any( GenericType.class ) ) )
			.then( ic -> {
				System.arraycopy( ic.getArguments(), 0, arguments, 0, 3 );
				return null;
			} );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		assertThat( arguments[1], instanceOf( Entity.class ) );

		final Entity e = (Entity) arguments[1];

		assertThat( e.getMediaType(), equalTo( new MediaType( "type", "subtype" ) ) );
	}

	@Test
	public void cookieParam() throws Exception
	{
		createAction( CookieParam.class, CookieParamAction::new );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		final Collection<Cookie> cookies = (Collection<Cookie>) readDeclaredField( req, "cookies", true );

		assertThat( cookies, hasSize( 1 ) );

		final Cookie cookie = cookies.iterator().next();

		verify( this.mc.bld, times( 1 ) ).cookie( same( cookie ) );
	}

	@Test
	public void formParam() throws Exception
	{
		createAction( FormParam.class, FormParamAction::new );

		final Object[] arguments = new Object[3];

		when( this.mc.bld.method( any( String.class ), any( Entity.class ), any( GenericType.class ) ) )
			.then( ic -> {
				System.arraycopy( ic.getArguments(), 0, arguments, 0, 3 );
				return null;
			} );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		assertThat( arguments[1], instanceOf( Entity.class ) );

		final Entity e = (Entity) arguments[1];
		final Form f = (Form) e.getEntity();

		final MultivaluedMap<String, String> m = f.asMap();

		assertThat( m, hasEntry( ANNOTATION_VALUE, asList( PARAM_VALUE ) ) );
		assertThat( e.getMediaType(), equalTo( MediaType.APPLICATION_FORM_URLENCODED_TYPE ) );
	}

	@Test
	public void headerParam() throws Exception
	{
		createAction( HeaderParam.class, HeaderParamAction::new );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		final MultivaluedMap<String, Object> headers = (MultivaluedMap<String, Object>) readDeclaredField( req, "headers", true );

		assertThat( headers, hasEntry( ANNOTATION_VALUE, asList( PARAM_VALUE ) ) );
		verify( this.mc.bld, times( 1 ) ).headers( same( headers ) );
	}

	@Test
	public void pathParam() throws Exception
	{
		createAction( PathParam.class, PathParamAction::new );

		when( this.mc.methodTarget.resolveTemplate( any( String.class ), any( String.class ), any( boolean.class ) ) ).thenReturn( this.mc.methodTarget );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		verify( this.mc.methodTarget, times( 1 ) ).resolveTemplate( eq( ANNOTATION_VALUE ), eq( PARAM_VALUE ), eq( true ) );
	}

	@Test
	public void produces() throws Exception
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, new String[] { ANNOTATION_VALUE } );
		final Produces ann = TypeFactory.annotation( Produces.class, map );

		addAction( new ProducesAction( ann, -1 ) );

		final String[] accepts = new String[1];

		when( this.mc.bld.accept( (String[]) any() ) )
			.then( ic -> {
				System.arraycopy( ic.getArguments(), 0, accepts, 0, 1 );

				return null;
			} );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		assertThat( asList( accepts ), hasItem( ANNOTATION_VALUE ) );
	}

	@Test
	public void queryParam() throws Exception
	{
		createAction( QueryParam.class, QueryParamAction::new );

		when( this.mc.methodTarget.queryParam( any( String.class ), any( String.class ) ) ).thenReturn( this.mc.methodTarget );

		final RestRequest req = this.met.request( mock( Interface.class ) );
		req.run();

		verify( this.mc.methodTarget, times( 1 ) ).queryParam( eq( ANNOTATION_VALUE ), eq( PARAM_VALUE ) );
	}

	private <A extends Annotation, X extends Action> void createAction( Class<A> annoType, BiFunction<A, ActionParam, X> create ) throws AnnotationFormatException
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, ANNOTATION_VALUE );
		final A ann = TypeFactory.annotation( annoType, map );
		final ActionParam param = new ActionParam( Action.HEAD, Object.class, new Annotation[0], x -> PARAM_VALUE, x -> (String) x );

		addAction( create.apply( ann, param ) );
	}

	private void addAction( Action action )
	{
		this.actions.add( action );

		Collections.sort( this.actions );
	}
}
