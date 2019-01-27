
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.Collection;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
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
public class InternalInterceptorsTest
{

	private static final String ANNOTATION_VALUE = "name";
	private static final String PARAM_VALUE = "value";

	private final MockClient mc = new MockClient();
	private LazyParamConverter<Object> cvt;
	private RestMethod met;

	@Before
	@SneakyThrows
	public void setUp()
	{
		final RestServiceInfo rsi = new RestServiceInfo( this.mc.rci, Interface.class );
		final RestMethodInfo rmi = new RestMethodInfo( rsi, Interface.class.getMethod( "get" ) );

		this.cvt = this.mc.rci.getConvertersFactory().getConverter( Object.class, new Annotation[0] );
		this.met = new RestMethod( rmi, this.mc.rci );
	}

	@Test
	@SneakyThrows
	public void consumes()
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, new String[] { "type/subtype" } );
		final Consumes ann = TypeFactory.annotation( Consumes.class, map );

		this.met.chain.add( new INTConsumes( ann ) );
		createInterceptor( FormParam.class, INTFormParam::new );

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
		createInterceptor( CookieParam.class, INTCookieParam::new );

		final RestRequestContext rc = callMock();
		final Collection<Cookie> cookies = rc.getCookies();

		assertThat( cookies, hasSize( 1 ) );

		cookies.forEach( c -> {
			verify( this.mc.bld, times( 1 ) ).cookie( same( c ) );
		} );
	}

	@Test
	public void formParam()
	{
		createInterceptor( FormParam.class, INTFormParam::new );

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
		createInterceptor( HeaderParam.class, INTHeaderParam::new );

		final RestRequestContext rc = callMock();
		final MultivaluedMap<String, String> headers = rc.getHeaders();

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
		createInterceptor( PathParam.class, INTPathParam::new );

		when( this.mc.target.resolveTemplate( any( String.class ), any( String.class ), any( boolean.class ) ) ).thenReturn( this.mc.target );

		callMock();

		verify( this.mc.target, times( 1 ) ).resolveTemplate( eq( ANNOTATION_VALUE ), eq( PARAM_VALUE ), eq( true ) );
	}

	@Test
	@SneakyThrows
	public void produces()
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, new String[] { MediaType.TEXT_HTML } );
		final Produces ann = TypeFactory.annotation( Produces.class, map );

		this.met.chain.add( new INTProduces( ann ) );

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
		createInterceptor( QueryParam.class, INTQueryParam::new );

		when( this.mc.target.queryParam( any( String.class ), any( String.class ) ) ).thenReturn( this.mc.target );

		callMock();

		verify( this.mc.target, times( 1 ) ).queryParam( eq( ANNOTATION_VALUE ), eq( PARAM_VALUE ) );
	}

	@SneakyThrows
	private RestRequestContext callMock()
	{
		final RestRequestContext[] rrc = new RestRequestContext[1];
		final RestRequestInterceptor rci = new RestRequestInterceptorBase()
		{

			@Override
			protected void before( RestRequestContext rc )
			{
				rrc[0] = rc;
			}

			@Override
			public int priority()
			{
				return PRIORITY_HEAD;
			}
		};
		this.met.chain.add( rci );
		this.met.request( mock( Interface.class ) );

		return rrc[0];
	}

	@SneakyThrows
	private <A extends Annotation, X extends RestRequestInterceptor> void createInterceptor( Class<A> annoType, BiFunction<A, RestParam, X> create )
	{
		final Map<String, Object> map = singletonMap( PARAM_VALUE, ANNOTATION_VALUE );
		final A ann = TypeFactory.annotation( annoType, map );
		final RestParam param = new RestParam( 0, Object.class, this.cvt, null, x -> PARAM_VALUE );

		this.met.chain.add( create.apply( ann, param ) );
	}
}
