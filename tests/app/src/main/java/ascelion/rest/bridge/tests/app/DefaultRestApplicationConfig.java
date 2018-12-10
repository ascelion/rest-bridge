
package ascelion.rest.bridge.tests.app;

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultRestApplicationConfig implements RestApplicationConfig
{

	@Override
	public void configure( Set<Class<?>> classes, Set<Object> singletons, Map<String, Object> properties )
	{
		classes.add( BeanIMPL.class );
		classes.add( BeanResourceImpl.class );
		classes.add( HelloImpl.class );
		classes.add( MethodsImpl.class );
		classes.add( ValidatedImpl.class );

		classes.add( JacksonResolver.class );
	}

}
