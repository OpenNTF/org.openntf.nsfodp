# NSF ODP Tooling

This project contains tooling for dealing with NSF on-disk-project representations in Maven and Eclipse.

There are three main components: a Maven plugin, a set of Domino OSGi plugins, and a set of Eclipse plugins. In tandem, they provide several features:

### ODP Compiler

The ODP compiler allows the use of a Domino server or local Notes installation to compile an on-disk project into a full NSF without the need of Domino Designer. This compilation supports classic design elements as well as XPages, and allows for using OSGi plugins to resolve classes and XPages components.

To use this for remote compilation, install the Domino plugins on an otherwise-clean Domino server - this is important to allow the plugins to be loaded and unloaded dynamically without interfering with existing plugins.

### ODP Exporter

The ODP exporter allows the use of a Domino server to export an NSF into a Designer-compatible ODP format.

### Eclipse Tooling

The Eclipse plugins provide the Eclipse IDE with basic knowledge of the ODP and autocompletion capabilities for XPages and Custom Controls.

Currently, autocompletion knows about the stock components and Extension Library that ship with 10.0.1 as well as any Custom Controls inside the same project.

Additionally, it adds "Compile On-Disk Project" and "Deploy NSF" actions to the context menu, which are shortcuts for the equivalent Maven goals.

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
                <version>3.0.0-SNAPSHOT</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
```

Additionally, there are some properties to set in your Maven `~/.m2/settings.xml` configuration.

There are two modes of operation: local and remote. In the case of local operations, set the `notes-program` to the path to a local Notes or Domino installation and `notes-platform` to the URL of a [Domino update site](https://github.com/OpenNTF/generate-domino-update-site). In practice, I've found that update sites generated from Domino instead of Notes are more reliable.

For server-based operations, there are properties for each task to set the remote server:

```xml
<?xml version="1.0"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <profiles>
        <profile>
            <id>nsfodp</id>
            <properties>
                <!-- for local operations, macOS example -->
                <notes-program>/Applications/IBM Notes.app/Contents/MacOS</notes-program>
                <notes-platform>file:///Users/username/path/to/Domino10.0.1</notes-platform>
              
                <!-- for remote operations -->
                <nsfodp.compiler.server>someserver</nsfodp.compiler.server>
                <nsfodp.compiler.serverUrl>https://some.server/</nsfodp.compiler.serverUrl>
                <nsfodp.exporter.server>someserver</nsfodp.exporter.server>
                <nsfodp.exporter.serverUrl>https://some.server/</nsfodp.exporter.serverUrl>
                <!-- Note: deployment operations currently require a server -->
                <nsfodp.deploy.server>someserver</nsfodp.deploy.server>
                <nsfodp.deploy.serverUrl>http://some.server/</nsfodp.deploy.serverUrl>
            </properties>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>nsfodp</activeProfile>
    </activeProfiles>
  
    <!-- needed when using remote execution -->
    <servers>
        <server>
            <id>someserver</id>
            <username>builduser</username>
            <password>buildpassword</password>
        </server>
    </servers>
</settings>
```

### ODP Compilation

The ODP compilation process has several properties that can be configured in the plugin's `configuration` block, with these as the defaults:

```xml
<configuration>
	<outputDirectory>${project.build.directory}</outputDirectory>
    <outputFileName>${project.build.finalName}.nsf</outputFileName>
    <odpDirectory>odp</odpDirectory>
    <!-- e.g. ../../releng/some.updatesite.project/target/site -->
    <updateSite></updateSite>
    <compilerLevel>1.8</compilerLevel>
    <!-- Adds the build timestamp to the generated NSF's title -->
    <appendTimestampToTitle>false</appendTimestampToTitle>
    <!-- Creates/updates a $TemplateBuild shared field -->
    <templateName></templateName>
    <!-- Enabled resource aggregation and compressed JS libs in xsp.properties -->
    <setProductionXspOptions>false</setProductionXspOptions>
    <!-- Add jars to the compilation classpath, to mimic jvm/lib/ext deployment -->
    <classpathJars>
        <classpathJar></classpathJar>
    </classpathJars>
</configuration>
```

### ODP Exporter

The ODP exporter is triggered manually, and does not require a Maven project in the current directory (though it will use the settings of an active project if present).

To export an ODP from the command line, execute the mojo directly:

```shell
mvn org.openntf.maven:nsfodp-maven-plugin:2.0.0:export-odp -DdatabasePath=names.nsf
```

This mojo will create or replace the `odp` directory in the current or project directory with the contents of the specified database. The directory path can be overridden by specifying the `nsfodp.exporter.odpDirectory` property in the execution.

This process also has several configuration options

```xml
<configuration>
    <odpDirectory>odp</odpDirectory>
    <!-- Enable https://openntf.org/main.nsf/project.xsp?r=project/Swiper -->
    <swiperFilter>true</swiperFilter>
    <!-- Export notes in "binary" note format, like the Designer option -->
    <binaryDxl>false</binaryDxl>
    <!-- Export rich text items as Base64 data instead of DXL-ified -->
    <richTextAsItemData>true</richTextAsItemData>
</configuration>
```

### NSF Deployment

To specify a deployment destination and path, expand your project's pom to include configuration information for deployment:

```xml
    ...
    <plugin>
        <groupId>org.openntf.maven</groupId>
        <artifactId>nsfodp-maven-plugin</artifactId>
        <version>2.0.0</version>
        <extensions>true</extensions>
        <configuration>
            <!-- This can be on the target Domino server a remote one -->
            <deployDestPath>someserver!!someapp.nsf</deployDestPath>
            <deployReplaceDesign>true</deployReplaceDesign>
        </configuration>
    </plugin>
    ...
```

By default, compilation binds to the `compile` phase and deployment binds to the `deploy` phase, when their parameters are specified.

## Requirements

### Maven

The Maven plugin requires Maven 3.0+ and Java 8+.

### Eclipse

The Eclipse plugin targets Neon and above, but may work with older releases, as long as they are launched in a Java 8+ runtime.

### Domino (For Server Operations)

The Domino plugins require Domino 9.0.1 FP10 or above. Additionally, it requires the [XPages Bazaar](https://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20Bazaar) version 2.0.3 or above.

### Notes or Domino (For Local Operations)

Local compilation and export require Notes or Domino 9.0.1 FP10 or above on Windows and Linux. On macOS, it requires Notes 10.0.1 or above.

Note: if you use local compilation, either your ID file should have no password or you should configure Notes's User Security to allow non-Notes-based programs to execute without prompting for a password.

#### Compilation on macOS

Due to the way the macOS Notes JVM is set up, the process currently requires that the running user have access to modify the application bundle, which is the default for admin users.

## License

This project is licensed under the Apache License 2.0.
