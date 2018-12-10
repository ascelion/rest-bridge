
package ascelion.rest.bridge;

import java.io.File;

import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;

public class ServersContainerImpl
extends DefaultPolymorphicDomainObjectContainer<Server> implements ServersContainer
{

	@Getter
	private final Class<?> glassfish = Glassfish.class;
	@Getter
	private final Class<?> payara = Glassfish.class;
	@Getter
	private final Project project;
	@Getter
	private File base;

	public ServersContainerImpl( Project project, Instantiator instantiator )
	{
		super( Server.class, instantiator );

		this.project = project;
		this.base = new File( project.getRootProject().getBuildDir(), "servers" );
	}

	public void base( Object base )
	{
		if( base instanceof File ) {
			this.base = (File) base;
		}
		else {
			final File temp = new File( base.toString() );

			this.base = temp.isAbsolute() ? temp : this.project.getRootProject().file( base );
		}
	}
}
