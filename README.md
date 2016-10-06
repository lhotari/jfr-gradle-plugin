# jfr-gradle-plugin

Gradle plugin for creating JFR logs for each build.

## Usage

### Installing the plugin

Add plugin to build script
```
plugins {
  id "io.github.lhotari.jfr" version "0.1"
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
        classpath "gradle.plugin.io.github.lhotari.jfr:jfr-gradle-plugin:0.1"
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

These JVM options are needed for JFR: `-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=1024 -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints`

One way to do this is to add the options to `org.gradle.jvmargs` key in `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2500m -Xverify:none -XX:+HeapDumpOnOutOfMemoryError -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=1024 -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
```