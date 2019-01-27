
package ascelion.rest.bridge.client;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import org.junit.Test;

public class RBUtilsTest
{

	@Test
	public void pathElements()
	{
		final Set<String> elements = RBUtils.pathParameters( "first/{path1}/{path2}/samd/{path3}/last" );

		assertThat( elements, hasSize( 3 ) );
		assertThat( elements, contains( "path1", "path2", "path3" ) );
	}

	@Test
	public void getRequestURINoStartSlash() throws AnnotationFormatException
	{
		final Map<String, Object> map = singletonMap( "value", "path1" );
		final Path ann = TypeFactory.annotation( Path.class, map );

		assertThat( RBUtils.getRequestURI( ann ), equalTo( "/path1" ) );
	}

	@Test
	public void getRequestURINoStartSlash2() throws AnnotationFormatException
	{
		final Map<String, Object> map = singletonMap( "value", "path1/" );
		final Path ann = TypeFactory.annotation( Path.class, map );

		assertThat( RBUtils.getRequestURI( ann ), equalTo( "/path1" ) );
	}

	@Test
	public void getRequestURI() throws AnnotationFormatException
	{
		final Map<String, Object> map = singletonMap( "value", "//path1///path2////" );
		final Path ann = TypeFactory.annotation( Path.class, map );

		assertThat( RBUtils.getRequestURI( ann ), equalTo( "/path1/path2" ) );
	}

	@Test
	public void getRequestURIEmpty() throws AnnotationFormatException
	{
		final Map<String, Object> map = singletonMap( "value", "///" );
		final Path ann = TypeFactory.annotation( Path.class, map );

		assertThat( RBUtils.getRequestURI( ann ), equalTo( "" ) );
	}

	@Test
	public void getRequestURINull() throws AnnotationFormatException
	{
		assertThat( RBUtils.getRequestURI( null ), equalTo( "" ) );
	}
}
