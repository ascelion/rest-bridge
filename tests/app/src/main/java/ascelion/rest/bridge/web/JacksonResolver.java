
package ascelion.rest.bridge.web;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Provider
public class JacksonResolver implements ContextResolver<ObjectMapper>
{

	private final ObjectMapper om = new ObjectMapper();

	public JacksonResolver()
	{
//		final SimpleModule sm = new SimpleModule( "user-info" );
//		final SimpleAbstractTypeResolver rz = new SimpleAbstractTypeResolver();
//
//		rz.addMapping( UserInfo.class, UserInfoImpl.class );
//		sm.setAbstractTypes( rz );
//
//		this.om.registerModule( sm );
	}

	@Override
	public ObjectMapper getContext( Class<?> type )
	{
		return this.om;
	}

}
