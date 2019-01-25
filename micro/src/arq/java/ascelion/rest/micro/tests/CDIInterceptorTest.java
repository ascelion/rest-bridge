
package ascelion.rest.micro.tests;

import javax.inject.Inject;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.assertEquals;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientWithURIAndInterceptor;
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
				ClientWithURIAndInterceptor.class, ReturnWithURLRequestFilter.class )
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
	private ClientWithURIAndInterceptor client;

	@Test
	public void testIMPL1()
	{
		LoggableInterceptor.setInvocationMessage( "" );

		assertThat( this.impl1.call(), equalTo( "HELO" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(), "" );
	}

	@Test
	public void testIMPL2()
	{
		LoggableInterceptor.setInvocationMessage( "" );

		assertThat( this.impl2.call(), equalTo( "HELO" ) );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			LogableIMPL2.class.getName() + ".call HELO" );
	}

	@Test
	public void testInterceptorInvoked() throws Exception
	{
		LoggableInterceptor.setInvocationMessage( "" );
		final String expectedResponse = "GET http://localhost:5017/myBaseUri/hello";
		assertEquals( this.client.get(), expectedResponse );

		assertEquals( LoggableInterceptor.getInvocationMessage(),
			ClientWithURIAndInterceptor.class.getName() + ".get " + expectedResponse );
	}

	@Test
	public void testInterceptorNotInvokedWhenNoAnnotationApplied() throws Exception
	{
		LoggableInterceptor.setInvocationMessage( "" );
		final String expectedResponse = "GET http://localhost:5017/myBaseUri/hello";
		assertEquals( this.client.getNoInterceptor(), expectedResponse );

		assertEquals( LoggableInterceptor.getInvocationMessage(), "" );
	}
}
