
package ascelion.rest.bridge

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.eclipse.GenerateEclipseProject
import org.gradle.plugins.ide.eclipse.model.ClasspathEntry
import org.gradle.util.ClosureBackedAction

class BuildPlugin implements Plugin<Project> {

	@Override
	public void apply( Project target ) {
		extendModel( target )

		target.plugins.apply( 'eclipse' )
		target.plugins.apply( 'java-library' )

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
					defaultOutputDir file("build/eclipse")

					sourceSets.all { SourceSet set ->
						plusConfigurations += [configurations.getByName( set.annotationProcessorConfigurationName )]
					}

					file {
						whenMerged {
							configureScope( entries, target.configurations.findByName( 'compileOnly' ) )

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

	private void extendModel( Project target ) {
		target.metaClass.stringProperty = { String key, Object val ->
			String prp = System.getProperty( key )

			if( prp != null ) {
				return prp
			}

			return target.ext.properties.getOrDefault( key, val )
		}

		target.metaClass.booleanProperty = { String key, boolean val ->
			String prp = System.getProperty( key )

			if( prp != null ) {
				return prp.empty || Boolean.valueOf( prp )
			}

			return Boolean.valueOf( target.stringProperty( key, val ) )
		}

		target.metaClass.integerProperty = { String key, int val ->
			return Integer.valueOf( target.stringProperty( key, Integer.toString( val )) )
		}

		target.metaClass.stringProperty = { String key ->
			target.stringProperty( key, null )
		}
		target.metaClass.booleanProperty = { String key ->
			target.booleanProperty( key, false )
		}
		target.metaClass.integerProperty = { String key ->
			target.integerProperty( key, 0 )
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

	private void configureScope( List<ClasspathEntry> entries, Configuration cfg ) {
		entries.findAll { it.kind == 'src' || it.kind == 'lib' }.each {
			if( it.entryAttributes.containsKey('gradle_used_by_scope')) {
				def scope = it.entryAttributes['gradle_used_by_scope']

				it.entryAttributes['test'] = !scope.contains('main') && !scope.empty
			}
		}
		if( cfg != null ) {
			entries.findAll { it.kind == 'lib' }.each {
				if( cfg.resolvedConfiguration.files.contains( new File( it.path ) ) ) {
					it.entryAttributes['test'] = false
				}
			}
		}
	}
}
