
package ascelion.rest.bridge.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import lombok.AccessLevel;
import lombok.Getter;

@ApplicationPath( API.BASE )
@ApplicationScoped
@Getter
public class RestApplication
extends Application
{

	private final Set<Class<?>> classes = new HashSet<>();
	private final Set<Object> singletons = new HashSet<>();
	private final Map<String, Object> properties = new HashMap<>();

	@Inject
	@Any
	@Getter( AccessLevel.NONE )
	private Instance<RestApplicationConfig> configs;

	@PostConstruct
	private void addClasses()
	{
		for( final RestApplicationConfig c : this.configs ) {
			c.configure( this.classes, this.singletons, this.properties );
		}
	}
}
