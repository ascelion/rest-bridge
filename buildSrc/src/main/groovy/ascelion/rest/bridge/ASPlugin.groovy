
package ascelion.rest.bridge

import javax.inject.Inject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.internal.reflect.Instantiator
import org.gradle.model.Model
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.RuleSource

class ASPlugin implements Plugin<Project> {
	static class ASRules extends RuleSource {

		@Model
		public ServersContainer containers( ExtensionContainer ext ) {
			return ext.getByType( ServersContainer.class )
		}

		@Mutate
		public void createTasks( ModelMap<Task> tasks, ServersContainer sc ) {
			sc.each { it.createTasks() }
		}
	}

	private final Instantiator instantiator

	@Inject
	ASPlugin( Instantiator instantiator ) {
		this.instantiator = instantiator
	}

	@Override
	public void apply( Project target ) {
		target.extensions.create( ServersContainer, "containers", ServersContainerImpl, target, this.instantiator )

		target.extensions.configure( ServersContainer ) { ServersContainer sc ->
			sc.registerFactory( Glassfish ) { name ->
				new Glassfish( name, sc, this.instantiator )
			}
		}
	}
}
