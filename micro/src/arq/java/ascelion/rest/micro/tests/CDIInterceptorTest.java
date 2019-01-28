
package ascelion.rest.micro.tests;

import javax.inject.Inject;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.assertEquals;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.Loggable;
import org.eclipse.microprofile.rest.client.tck.interfaces.LoggableInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CDIInterceptorTest extends Arquillian
{

	@Deployment
	public static WebArchive createDeployment()
	{
		final String simpleName = CDIInterceptorTest.class.getSimpleName();
		final Asset beansXML = new ClassLoaderAsset( "beans-1.0.xml" );
		final JavaArchive jar = ShrinkWrap.create( JavaArchive.class, simpleName + ".jar" )
			.addClasses(
				LogableITF.class, LogableIMPL1.class, LogableIMPL2.class,
				Loggable.class, LoggableInterceptor.class,
				Secured.class, SecuredInterceptor.class,
				BothInterceptors.class,
				ClientWithTwoInterceptors.class, ReturnWithURLRequestFilter.class )
			.addAsManifestResource( beansXML, "beans.xml" );
		return ShrinkWrap.create( WebArchive.class, simpleName + ".war" )
			.addAsLibrary( jar )
			.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private LogableITF impl1;
	@Inject
	private LogableIMPL2 impl2;
	@Inject
	@RestClient
	private ClientWithTwoInterceptors client;

	@BeforeMethod
	public void setUp()
	{
		LoggableInterceptor.setInvocationMessage( "" );
		SecuredInterceptor.invoked.clear();
	}

	@Test
	public void testIMPL1()
	{
		assertThat( this.impl1.call(), equalTo( "HELO" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(), "" );
	}

	@Test
	public void testIMPL2()
	{
		assertThat( this.impl2.call(), equalTo( "HELO" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			LogableIMPL2.class.getName() + ".call HELO" );
	}

	@Test
	public void testGetLoggable() throws Exception
	{
		final String expectedResponse = "GET " + ClientWithTwoInterceptors.URI;
		assertEquals( this.client.getLoggable(), expectedResponse );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			ClientWithTwoInterceptors.class.getName() + ".getLoggable " + expectedResponse );
	}

	@Test
	public void testGetSecured() throws Exception
	{
		final String expectedResponse = "GET " + ClientWithTwoInterceptors.URI;
		assertEquals( this.client.getSecured(), expectedResponse );

		assertThat( SecuredInterceptor.invoked, hasSize( 1 ) );
		assertThat( SecuredInterceptor.invoked, contains( "" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(), "" );
	}

	@Test
	public void testGetTwoWithValue() throws Exception
	{
		final String expectedResponse = "GET " + ClientWithTwoInterceptors.URI;
		assertEquals( this.client.getTwoWithValue(), expectedResponse );

		assertThat( SecuredInterceptor.invoked, hasSize( 1 ) );
		assertThat( SecuredInterceptor.invoked, contains( "with-value" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			ClientWithTwoInterceptors.class.getName() + ".getTwoWithValue " + expectedResponse );
	}

	@Test
	public void testGetWithBoth() throws Exception
	{
		final String expectedResponse = "GET " + ClientWithTwoInterceptors.URI;
		assertEquals( this.client.getWithBoth(), expectedResponse );

		assertThat( SecuredInterceptor.invoked, hasSize( 1 ) );
		assertThat( SecuredInterceptor.invoked, contains( "two" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			ClientWithTwoInterceptors.class.getName() + ".getWithBoth " + expectedResponse );
	}

	@Test
	public void testGetWithAll() throws Exception
	{
		final String expectedResponse = "GET " + ClientWithTwoInterceptors.URI;
		assertEquals( this.client.getWithAll(), expectedResponse );

		assertThat( SecuredInterceptor.invoked, hasSize( 2 ) );
		assertThat( SecuredInterceptor.invoked, contains( "all", "two" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			ClientWithTwoInterceptors.class.getName() + ".getWithAll " + expectedResponse );
	}

	@Test
	public void testInterceptorNotInvokedWhenNoAnnotationApplied() throws Exception
	{
		final String expectedResponse = "GET " + ClientWithTwoInterceptors.URI;
		assertEquals( this.client.getNoInterceptor(), expectedResponse );

		assertEquals( LoggableInterceptor.getInvocationMessage(), "" );
	}
}
