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
