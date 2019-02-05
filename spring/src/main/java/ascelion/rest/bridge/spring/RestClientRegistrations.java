
package ascelion.rest.bridge.spring;

import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;

import ascelion.rest.bridge.client.RestClient;
import ascelion.utils.etc.TypeDescriptor;

import static java.util.Arrays.stream;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass( {
	ClientBuilder.class,
	RestClient.class,
} )
public class RestClientRegistrations implements BeanDefinitionRegistryPostProcessor, BeanClassLoaderAware
{

	private ClassLoader cld;

	@Override
	public void setBeanClassLoader( ClassLoader cld )
	{
		this.cld = cld;
	}

	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException
	{
	}

	@Override
	public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException
	{
		stream( registry.getBeanDefinitionNames() )
			.sorted()
			.forEach( n -> {
				final BeanDefinition beanDefinition = registry.getBeanDefinition( n );
				final String beanClassName = beanDefinition.getBeanClassName();

				if( beanClassName != null ) {
					try {
						final TypeDescriptor t = new TypeDescriptor( this.cld.loadClass( beanClassName ) );

						t.getProperties().forEach( p -> {
							if( p.isAnnotationPresent( Autowired.class ) && p.getType().isAnnotationPresent( Path.class ) ) {
								register( registry, p.getType(), p.getAnnotation( Qualifier.class ) );
							}
						} );
					}
					catch( final ClassNotFoundException e ) {
						throw new RuntimeException( "Cannot load " + beanClassName, e );
					}
				}

			} );
	}

	private void register( BeanDefinitionRegistry reg, Class<?> type, Qualifier qual )
	{
		final String name = type.getName() + ( qual != null ? ( "-" + qual.value() ) : "" );

		if( !reg.containsBeanDefinition( name ) ) {
			final AbstractBeanDefinition def = genericBeanDefinition( RestClientFactory.class )
				.addConstructorArgValue( type )
				.addConstructorArgValue( qual )
				.setLazyInit( true )
				.getBeanDefinition();

			if( qual != null ) {
				def.addQualifier( new AutowireCandidateQualifier( Qualifier.class, qual.value() ) );
			}
			else {
				def.setPrimary( true );
			}

			reg.registerBeanDefinition( name, def );
		}
	}
}
