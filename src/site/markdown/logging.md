# Custom Logging

It is possible to use an alternate logging configuration without modifying the application. This can be useful to change
the verbosity of the log or obtain other useful information. You need to be familiar with
[log4j2 configuration files](https://logging.apache.org/log4j/2.x/manual/configuration.html) in order to make use of this
facility.

Download the
[default log4j2 configuration file](https://raw.githubusercontent.com/x2vc/x2vc/main/src/main/resources/default-log4j2.xml)
from the repository or extract it from the application and place it in an arbitrary location. Edit the file as required - an
example for additional debugging output for certain components is included as a comment in the file. The `Routing` appender is
used to direct the log information to the console (for all messages that can not be assigned to an XSLT file) or to the
individual log file. The `Report` appender is an in-memory loopback to include the messages in the generated report.

The configuration file is specified using the option `--logConfig`. Assuming that the local copy of the file is named
`alt-log4j2.xml`, you can call x2vc with

```
$ x2vc <mode> <file(s)> --logConfig path/to/alt-log4j2.xml [otherOptions]
```

This will cause x2vc to use the alternate configuration file instead of the default file contained in the application.
