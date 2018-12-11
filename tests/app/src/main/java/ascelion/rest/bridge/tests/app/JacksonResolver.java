
package ascelion.rest.bridge.tests.app;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
@ApplicationScoped
public class JacksonResolver implements ContextResolver<ObjectMapper>
{

	@Produces
	private final ObjectMapper om = new ObjectMapper();

	public JacksonResolver()
	{
		this.om.enable( SerializationFeature.INDENT_OUTPUT );
	}

	@Override
	public ObjectMapper getContext( Class<?> type )
	{
		return this.om;
	}

}
