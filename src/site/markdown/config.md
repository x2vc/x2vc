# Configuration

x2vc provides various options to control its operation. The options are listed and documented in the
[default configuration file](https://github.com/x2vc/x2vc/blob/main/src/main/resources/application.conf).
There are two ways to override these defaults: specifying individual options or providing an additional configuration file.

## Setting individual options

To change the value of an option, use the command line parameter `-D<name>=<value>`. For example, to limit the maximum number of threads to 4, use

```
$ x2vc <mode> <file(s)> -Dx2vc.threads.max_count=4 [otherOptions]
```

## Configuration file

An additional configuration file can be specified by setting the parameter `config.file` to point to the alternate configuration
file:

```
$ x2vc <mode> <file(s)> -Dconfig.file=path/to/config-file [otherOptions]
```

Settings in the alternate configuration file overwrite the default settings; of no entry is present in the alternate file,
the default value is used. If you decide to use the
[default configuration file](https://raw.githubusercontent.com/x2vc/x2vc/main/src/main/resources/application.conf)
as a starting point, it is recommended to first **comment out all lines** and then selectively re-enable and change the values
you're interested in. This makes it easy to identify the actual changes and lets you benefit from updated default values
in the future.

