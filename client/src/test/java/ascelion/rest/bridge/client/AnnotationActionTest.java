
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static ascelion.rest.bridge.client.RestClientProperties.NO_ASYNC_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_REQUEST_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_RESPONSE_HANDLER;
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

import io.leangen.geantyref.TypeFactory;
import lombok.SneakyThrows;
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
	@SneakyThrows
	public void setUp()
	{
		final ConvertersFactory cvsf = new ConvertersFactory( this.mc.client );
		final RestClientData rcd = new RestClientData(	Interface.class, this.mc.configuration,
														cvsf, NO_REQUEST_INTERCEPTOR, NO_RESPONSE_HANDLER, null,
														NO_ASYNC_INTERCEPTOR, () -> this.mc.methodTarget );

		this.met = new RestMethod( rcd, Interface.class.getMethod( "get" ) );
		this.actions = (List<Action>) readDeclaredField( this.met, "actions", true );

		this.actions.clear();
	}

	@Test
	@SneakyThrows
	public void consumes()
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, new String[] { "type/subtype" } );
		final Consumes ann = TypeFactory.annotation( Consumes.class, map );

		createAction( FormParam.class, FormParamAction::new );
		addAction( new ConsumesAction( ann, -1 ) );

		final Object[] arguments = new Object[2];

		when( this.mc.bld.method( any( String.class ), any( Entity.class ) ) )
			.then( ic -> {
				System.arraycopy( ic.getArguments(), 0, arguments, 0, arguments.length );

				return this.mc.rsp;
			} );

		callMock();

		assertThat( arguments[1], instanceOf( Entity.class ) );

		final Entity e = (Entity) arguments[1];

		assertThat( e.getMediaType(), equalTo( new MediaType( "type", "subtype" ) ) );
	}

	@Test
	@SneakyThrows
	public void cookieParam()
	{
		createAction( CookieParam.class, CookieParamAction::new );

		final RestRequest<?> req = callMock();
		final Collection<Cookie> cookies = req.rc.getCookies();

		assertThat( cookies, hasSize( 1 ) );

		cookies.forEach( c -> {
			verify( this.mc.bld, times( 1 ) ).cookie( same( c ) );
		} );
	}

	@Test
	public void formParam()
	{
		createAction( FormParam.class, FormParamAction::new );

		final Object[] arguments = new Object[2];

		when( this.mc.bld.method( any( String.class ), any( Entity.class ) ) )
			.then( ic -> {
				System.arraycopy( ic.getArguments(), 0, arguments, 0, arguments.length );

				return this.mc.rsp;
			} );

		callMock();

		assertThat( arguments[1], instanceOf( Entity.class ) );

		final Entity e = (Entity) arguments[1];
		final Form f = (Form) e.getEntity();

		final MultivaluedMap<String, String> m = f.asMap();

		assertThat( m, hasEntry( ANNOTATION_VALUE, asList( PARAM_VALUE ) ) );
		assertThat( e.getMediaType(), equalTo( MediaType.APPLICATION_FORM_URLENCODED_TYPE ) );
	}

	@Test
	@SneakyThrows
	public void headerParam()
	{
		createAction( HeaderParam.class, HeaderParamAction::new );

		final RestRequest<?> req = callMock();
		final MultivaluedMap<String, String> headers = req.rc.getHeaders();

		assertThat( headers, hasEntry( ANNOTATION_VALUE, asList( PARAM_VALUE ) ) );

		headers.forEach( ( k, v ) -> {
			v.forEach( x -> {
				verify( this.mc.bld, times( 1 ) ).header( eq( k ), eq( x ) );
			} );
		} );
	}

	@Test
	public void pathParam()
	{
		createAction( PathParam.class, PathParamAction::new );

		when( this.mc.methodTarget.resolveTemplate( any( String.class ), any( String.class ), any( boolean.class ) ) ).thenReturn( this.mc.methodTarget );

		callMock();

		verify( this.mc.methodTarget, times( 1 ) ).resolveTemplate( eq( ANNOTATION_VALUE ), eq( PARAM_VALUE ), eq( true ) );
	}

	@Test
	@SneakyThrows
	public void produces()
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, new String[] { MediaType.TEXT_HTML } );
		final Produces ann = TypeFactory.annotation( Produces.class, map );

		addAction( new ProducesAction( ann, -1 ) );

		final MediaType[] accepts = new MediaType[1];

		when( this.mc.bld.accept( (MediaType[]) any() ) )
			.then( ic -> {
				System.arraycopy( ic.getArguments(), 0, accepts, 0, 1 );

				return null;
			} );

		callMock();

		assertThat( asList( (Object[]) accepts ), hasItem( MediaType.TEXT_HTML_TYPE ) );
	}

	@Test
	public void queryParam()
	{
		createAction( QueryParam.class, QueryParamAction::new );

		when( this.mc.methodTarget.queryParam( any( String.class ), any( String.class ) ) ).thenReturn( this.mc.methodTarget );

		callMock();

		verify( this.mc.methodTarget, times( 1 ) ).queryParam( eq( ANNOTATION_VALUE ), eq( PARAM_VALUE ) );
	}

	@SneakyThrows
	private <T extends Callable<?>> T callMock()
	{
		final Callable<T> req = (Callable<T>) this.met.request( mock( Interface.class ) );

		req.call();

		return (T) req;
	}

	@SneakyThrows
	private <A extends Annotation, X extends Action> void createAction( Class<A> annoType, BiFunction<A, ActionParam, X> create )
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
