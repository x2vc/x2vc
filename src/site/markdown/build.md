# Checkout and Build

## Command line

In addition to the [general requirements](installation.html), a recent version of Maven 3 as well as Git
is required. Assuming the prerequisites are met, the build and installation can then be performed with the following steps:

```
$ git clone https://github.com/x2vc/x2vc.git
$ cd x2vc
$ mvn clean verify
```

The installation package then resides in the `target` folder and can be extracted from there and
[used as described](usage.html).

**Note on test failures:** At the time of this writing, some of the automated tests that cover random value generation use
an averaging approach to verify the correct function of the implementations under test. Due to the random nature of the
test data generated, this approach is prone to fail every now and then (see [issue 64](https://github.com/x2vc/x2vc/issues/64)).
This results in the build process terminating with an error message during the testing phase. Until this issue is addressed, a
simple repetition of the `mvn clean verify` command shown above will usually complete successfully.

## Using Eclipse as IDE

First clone the repository to a desired location, using the option to discover projects automatically. This will result in a new
project named `x2vc` in your workspace. Right-click on the file `pom.xml` and choose *Run As > Maven install*.

To execute or debug x2vc, create a new run configuration of type Java application with the following settings:

 * select `org.x2vc.Checker` as main class
 * set the working directory to `${workspace_loc:x2vc/examples/manual/with-schema}` to execute the XSS samples
 * set command line arguments to `xss po_${string_prompt:XSLT file name (po_<VALUE>.xslt)}.xslt` -
   this will cause a prompt to appear when launching, allowing you to specify the file or files to be checked

