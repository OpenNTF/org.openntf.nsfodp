# NSF ODP Tooling

This project contains tooling for dealing with NSF on-disk-project representations in Maven and Eclipse.

There are three main components: a Maven plugin, a set of Domino OSGi plugins, and a set of Eclipse plugins. In tandem, they provide two main features:

### ODP Compiler

The ODP compiler allows the use of a Domino server to compile an on-disk project into a full NSF without the need of Domino Designer. This compilation supports classic design elements as well as XPages, and allows for using OSGi plugins to resolve classes and XPages components.

To use this, install the Domino plugins on an otherwise-clean Domino server - this is important to allow the plugins to be loaded and unloaded dynamically without interfering with existing plugins.

### NSF Deployment

The NSF deployment service allows for deployment of an NSF to a Domino server without involving the Notes client. Currently, this will only deploy new databases, but the plan is to have this also be able to perform a design replace on an existing database.

## Usage

To use this tooling with an ODP, wrap it in a Maven project with the `domino-nsf` packaging type. Here is an example pom:

```xml
<?xml version="1.0"?>
<project
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
	xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.example</groupId>
	<artifactId>example-nsf</artifactId>
    <version>1.0.0-SNAPSHOT</version>
	<packaging>domino-nsf</packaging>

    <pluginRepositories>
		<pluginRepository>
			<id>artifactory.openntf.org</id>
			<name>artifactory.openntf.org</name>
			<url>https://artifactory.openntf.org/openntf</url>
		</pluginRepository>
    </pluginRepositories>
    
    <build>
        <plugins>
			<plugin>
				<groupId>org.openntf.maven</groupId>
				<artifactId>nsfodp-maven-plugin</artifactId>
				<version>1.0.0-SNAPSHOT</version>
				<extensions>true</extensions>
			</plugin>
        </plugins>
    </build>
</project>
```

Then, add `nsfodp.compiler.server` and `nsfodp.compiler.serverUrl` properties to your Maven settings to reference a server ID and the base URL for your server running the OSGi plugins, along with a `server` entry for the server ID:

```xml
<?xml version="1.0"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<profiles>
		<profile>
			<id>main</id>
			<properties>
				<nsfodp.compiler.server>someserver</nsfodp.compiler.server>
				<nsfodp.compiler.serverUrl>http://some.server/</nsfodp.compiler.serverUrl>
			</properties>
        </profile>
    </profiles>
    <activeProfiles>
        	<activeProfile>main</activeProfile>
    </activeProfiles>
    <servers>
        <server>
			<id>someserver</id>
			<username>builduser</username>
			<password>buildpassword</password>
		</server>
    </servers>
</settings>
```

For deployment, add `nsfodp.deploy.server` and and `nsfodp.deploy.serverUrl` properties in the same manner. Additionally, expand your project's pom to include configuration information for deployment:

```xml
			...
			<plugin>
				<groupId>org.openntf.maven</groupId>
				<artifactId>nsfodp-maven-plugin</artifactId>
				<version>1.0.0-SNAPSHOT</version>
				<extensions>true</extensions>
				<configuration>
					<!-- This can be on the target Domino server a remote one -->
					<deployDestPath>someserver!!someapp.nsf</deployDestPath>
					<deployReplaceDesign>true</deployReplaceDesign>
				</configuration>
			</plugin>
			...
```

## Requirements

### Maven

The Maven plugin requires Maven 3.0+ and Java 8+.

### Eclipse

The Eclipse plugin targets Neon and above, but may work with older releases, as long as they are launched in a Java 8+ runtime.

### Domino

The Domino plugins require Domino 9.0.1 FP10 or above. Additionally, it requires the [XPages Bazaar](https://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20Bazaar) version 2.0.0 or above.

## License

This project is licensed under the Apache License 2.0.