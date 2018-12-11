package ascelion.rest.bridge

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ClosureBackedAction

abstract class Server implements Named {

	final String name
	final Project project

	private final ServersContainer sc
	private final Instantiator instantiator

	int adminPort
	String distribution
	boolean enabled = true

	Server( String name, ServersContainer sc, Instantiator instantiator ) {
		this.name = name
		this.sc = sc
		this.project = sc.project
		this.instantiator = instantiator
	}

	abstract protected File getExecutable()
	abstract boolean isServerUp()

	File getHome() {
		return new File( sc.base, name )
	}

	String getType() {
		return getClass().getSimpleName().toLowerCase()
	}

	void distribution( String distribution ) {
		this.distribution = distribution
	}

	protected final void createTasks() {
		createTask( 'install', null) { configureInstall( it ) }
		createTask( 'start', "Start ${this.name} instance") {  configureStart( it ) }
		createTask( 'stop', "Stop ${this.name} instance") { configureStop( it ) }
	}

	protected final void createTask(String taskName, String description, Closure<Task> closure) {
		this.project.tasks.create("${name}-${taskName}") { Task t ->
			t.group = 'servers'
			t.description = description
			t.onlyIf { this.enabled }

			new ClosureBackedAction<>( closure ).execute( t )
		}
	}

	protected final void addTaskDependency( Task second, String taskName ) {
		this.project.tasks.all { Task first ->
			if( first.name == "${this.name}-${taskName}" ) {
				second.dependsOn( first )
			}
		}
	}

	protected final void mustRunAfter( Task second, String taskName ) {
		this.project.tasks.all { Task first ->
			if( first.name == "${this.name}-${taskName}" ) {
				second.mustRunAfter( first )
			}
		}
	}

	protected void configureInstall(Task task) {
		Configuration dist = this.project.configurations.create( "${this.name}-dist" )

		this.project.dependencies.add( dist.name, this.distribution )

		task.inputs.files( dist )
		task.outputs.file( this.executable )

		String dest

		if( this.home.absolutePath.startsWith( this.project.rootDir.absolutePath ) ) {
			dest = '${rootDir}/' + this.project.rootProject.relativePath( this.home )
		}
		else {
			dest = this.home.absolutePath
		}

		task.description = "Install server ${this.name} into ${dest}"

		task.onlyIf {
			!this.executable.exists()
		}
		task.doLast {
			this.project.copy {
				FileTree tree = this.project.zipTree( dist.singleFile )

				from( tree )
				into( home )

				includeEmptyDirs = false

				eachFile {
					path = path.replaceFirst("^[^/]+/", "")
				}
			}
		}
	}

	protected void configureStart(Task task) {
		addTaskDependency( task, "install" )

		task.onlyIf { !this.serverUp }
	}

	protected void configureStop(Task task) {
		task.onlyIf { this.serverUp }
	}
}
