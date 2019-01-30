
package ascelion.rest.micro;

import java.util.Map;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RestBridgeFeatureContext implements FeatureContext
{

	private final Configurable<?> cfg;

	@Override
	public Configuration getConfiguration()
	{
		return this.cfg.getConfiguration();
	}

	@Override
	public FeatureContext property( String name, Object value )
	{
		this.cfg.property( name, value );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass )
	{
		this.cfg.register( componentClass );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass, int priority )
	{
		this.cfg.register( componentClass, priority );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass, Class<?>... contracts )
	{
		this.cfg.register( componentClass, contracts );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		this.cfg.register( componentClass, contracts );

		return this;
	}

	@Override
	public FeatureContext register( Object component )
	{
		this.cfg.register( component );

		return this;
	}

	@Override
	public FeatureContext register( Object component, int priority )
	{
		this.cfg.register( component, priority );

		return this;
	}

	@Override
	public FeatureContext register( Object component, Class<?>... contracts )
	{
		this.cfg.register( component, contracts );

		return this;
	}

	@Override
	public FeatureContext register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.cfg.register( component, contracts );

		return this;
	}

}
