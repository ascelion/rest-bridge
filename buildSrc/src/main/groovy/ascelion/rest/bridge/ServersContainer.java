
package ascelion.rest.bridge;

import java.io.File;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Project;

public interface ServersContainer
extends ExtensiblePolymorphicDomainObjectContainer<Server>
{

	Project getProject();

	File getBase();
}
