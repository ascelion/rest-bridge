
package ascelion.rest.bridge.tests.app;

import java.util.Map;
import java.util.Set;

public interface RestApplicationConfig
{

	void configure( Set<Class<?>> classes, Set<Object> singletons, Map<String, Object> properties );

}
