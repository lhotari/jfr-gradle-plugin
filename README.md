# jfr-gradle-plugin

Gradle plugin for creating JFR logs for each build. JFR has shipped with Oracle Java since 1.7.0_40 .

## Usage

### Installing the plugin

Add plugin to build script
```
plugins {
  id "io.github.lhotari.jfr" version "0.2"
}
```

### Use plugin for all builds

Copy [`jfr-init.gradle`](jfr-init.gradle) to `~/.gradle/init.d` directory

The contents of `jfr-init.gradle`:
```
initscript {
    repositories {
      maven {
          url "https://plugins.gradle.org/m2"
        }
    }
    dependencies {
        classpath "gradle.plugin.io.github.lhotari.jfr:jfr-gradle-plugin:0.2"
    }
}
apply plugin: io.github.lhotari.jfr.JfrProfilingPlugin
```

You can customize initialization to take effect only when some condition is met.

For example, you could use an environment variable to activate profiling.

```
if(System.getenv('GRADLE_PROFILING_ENABLED')) {
    apply plugin: io.github.lhotari.jfr.JfrProfilingPlugin
}
```

### Enabling JFR for the Gradle JVM

These JVM options are needed for JFR: `-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=1024 -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints`.

These options [are recommended in the JFR manual](https://docs.oracle.com/javacomponents/jmc-5-5/jfr-runtime-guide/about.htm#JFRRT111).

> Note that when running alternative languages relying on lambda forms on the JVM -- such as the JavaScript implementation Nashorn -- the depths of the stack traces can get quite deep. To ensure that stack traces with large stacks are sampled properly, you may need to increase the Flight Recorder stack depth. Setting its value to 1024 will usually be enough:

> `java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=1024 MyApp`

---

> One nice property of the JFR method profiler is that it does not require threads to be at safe points in order for stacks to be sampled. However, since the common case is that stacks will only be walked at safe points, HotSpot normally does not provide metadata for non-safe point parts of the code, which means that such samples will not be properly resolved to the correct line number and BCI. That is, unless you specify:

> `-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints`

> With DebugNonSafepoints, the compiler will generate the necessary metadata for the parts of the code not at safe points as well.


One way to do this is to add the options to `org.gradle.jvmargs` key in `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2500m -Xverify:none -XX:+HeapDumpOnOutOfMemoryError -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=1024 -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
```

### Java Mission Control UI for analysing the JFR logs

```
jmc -open [jfr log file.jfr]
```

### Flamegraphs

With the help of [jfr-report-tool](https://github.com/lhotari/jfr-report-tool), you can make CPU [Flamegraphs](http://www.brendangregg.com/flamegraphs.html).