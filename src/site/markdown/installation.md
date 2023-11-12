# Installation

## System requirements

x2vc relies on a relatively new version of Java - currently, at least Java 20 is required. If you do not have a suitable
version installed, you can download the latest GA version from the [OpenJDK project site](https://openjdk.org/projects/jdk/)
or use the [Eclipse Temurin](https://adoptium.net/) installers provided by Adoptium. Other methods of obtaining a suitable Java
version may be available depending on your platform (Homebrew, Linux package managers, ...).

You do not need to install the Java version system-wide - it is sufficient to configure it locally by setting the appropriate
environment variables. `JAVA_HOME` has to be set to the installation base of the Java Runtime Environment (JRE), and the Java
executable has to be available (check with `java --version`).

All other dependencies are contained in the installation package. x2vc is a headless CLI application that should be executable
basically anywhere you can get Java running.

## Download and "install"

Download the latest release from the [releases page](https://github.com/x2vc/x2vc/releases) over on GitHub. Unzip the folder
and place the resulting folder `x2vc-checker-VERSION` wherever convenient. You do not need to move it to a system folder, and
you do not need administrative privileges to install it.

For continued use, it is advisable to add `path/to/x2vc-checker-VERSION/bin` to the search path (i.e., the `PATH` variable on most
platforms) so that you can call `x2vc` from anywhere. If you just want to try the tool out, you can simply specify the full path
for every invocation.

At this point, you should be able to run `x2vc` on the command line and get a short usage info.
