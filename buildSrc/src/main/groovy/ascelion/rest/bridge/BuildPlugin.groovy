
package ascelion.rest.bridge

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.eclipse.GenerateEclipseProject
import org.gradle.plugins.ide.eclipse.model.ClasspathEntry
import org.gradle.util.ClosureBackedAction

class BuildPlugin implements Plugin<Project> {

	@Override
	public void apply( Project target ) {
		target.plugins.apply( 'eclipse' )
		target.plugins.apply( 'java' )

		target.sourceSets.all { SourceSet set ->
			set.output.resourcesDir = set.output.classesDirs.singleFile
		}

		ClosureBackedAction.execute(target) {
			eclipse {
				project {
					name = "${rootProject.name}${target.path.replace(':', '-')}"

					natures 'org.eclipse.buildship.core.gradleprojectnature'
					buildCommand 'org.eclipse.buildship.core.gradleprojectbuilder'
				}

				classpath {
					defaultOutputDir target.file("build/eclipse")

					file {
						whenMerged {
							configureScope( entries )

							target.sourceSets.each { SourceSet set ->
								configureJava( target, entries, set )
								configureResources( target, entries, set )
							}
						}
					}
				}
			}
		}

		target.tasks.withType( GenerateEclipseProject ) { Task task ->
			target.sourceSets.all { SourceSet set ->
				task.finalizedBy set.processResourcesTaskName
			}
		}
	}

	private void configureJava( Project target, List<ClasspathEntry> entries, SourceSet set) {
		String dest = target.relativePath( set.output.classesDirs.singleFile )

		set.allJava.srcDirs.each { File src ->
			String path = target.relativePath( src )

			entries
					.findAll { it.kind == 'src' && it.path == path }
					.each { it.output = dest }
		}
	}

	private void configureResources( Project target, List<ClasspathEntry> entries, SourceSet set) {
		String dest = target.relativePath( set.output.resourcesDir )

		set.resources.srcDirs.each { File src ->
			String path = target.relativePath( src )

			entries
					.findAll { it.kind == 'src' && it.path == path }
					.each { it.output = dest }
		}
	}

	private void configureScope( List<ClasspathEntry> entries) {
		entries.findAll { it.kind == 'src' }.each {
			if( it.entryAttributes.containsKey('gradle_scope')) {
				def scope = it.entryAttributes['gradle_scope']

				it.entryAttributes['test'] = scope == 'test' || scope == 'arquillian'
			}
		}
	}
}
