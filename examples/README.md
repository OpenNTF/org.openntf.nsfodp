# Examples

These examples demonstrate different configurations for using the NSF ODP Tooling

## Single NSF

The `single-nsf` example demonstrates the basic case: a single-module Maven project containing a basic NSF (in this case, a newly-created empty NSF).

## XPages Library

The `xpages-library` example shows the case of a multi-module, Tycho-based Maven project that includes an OSGi plugin providing an XPages library, and then an NSF that makes use of it. It demonstrates:

- A Maven module hierarchy
- The use of Tycho to build OSGi applications
- Extended NSF ODP configuration options, such as a pom-defined ACL, a template name, and OSGi and Maven dependencies
- A distribution project in "releng" to create a ZIP of the update site and NSF