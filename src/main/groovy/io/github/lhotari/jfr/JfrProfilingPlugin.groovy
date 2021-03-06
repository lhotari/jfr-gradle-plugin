package io.github.lhotari.jfr

import groovy.transform.CompileStatic
import org.gradle.StartParameter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.initialization.BuildCompletionListener

import static io.github.lhotari.jfr.JfrControl.*

@CompileStatic
class JfrProfilingPlugin implements Plugin<Object> {
    static final String JFR_JVM_OPTS = '-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=1024 -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints'
    JfrProfilingPlugin() {

    }

    @Override
    void apply(Object target) {
        if (!jfrEnabled()) {
            System.err.println("JFR is only available on Oracle JVM 1.7.0_40+.")
            System.err.println("Add ${JFR_JVM_OPTS} to the JVM options (org.gradle.jvmargs) of gradle.")
            return
        }

        if (target instanceof Project) {
            applyToProject(target)
        } else if (target instanceof Settings) {
            applyToSettings(target)
        } else if (target instanceof Gradle) {
            applyToGradle(target)
        }
    }

    void applyToGradle(Gradle gradle) {
        startProfilingAndAddListener(gradle.startParameter.projectDir ?: gradle.startParameter.currentDir, gradle)
    }

    void applyToProject(Project project) {
        if (project != project.rootProject) {
            project.logger.warn("Applying this plugin is only supported for the root project.")
            return
        }
        def dumpInfo = new JfrDumpInfo(project.rootDir, project.gradle.startParameter)
        if(!dumpInfo.isProfiling()) {
            startProfiling(dumpInfo)
            project.gradle.addListener(createBuildCompletionListener(dumpInfo))
        }
    }

    private BuildCompletionListener createBuildCompletionListener(JfrDumpInfo dumpInfo) {
        new BuildCompletionListener() {
            @Override
            void completed() {
                stopProfiling(dumpInfo)
            }
        }
    }

    private void stopProfiling(JfrDumpInfo dumpInfo) {
        try {
            def jfrLogFile = dumpInfo.createRecordingFile()
            System.out.println("Dumping JFR log to ${jfrLogFile}...")
            System.out.println(jfrStop(dumpInfo.recordingName, jfrLogFile))
            System.out.println("Done.")
        } finally {
            dumpInfo.clearStatus()
        }
    }

    void applyToSettings(Settings settings) {
        startProfilingAndAddListener(settings.rootDir, settings.gradle)
    }

    private void startProfilingAndAddListener(File rootDir, Gradle gradle) {
        def dumpInfo = new JfrDumpInfo(rootDir, gradle.startParameter)
        startProfiling(dumpInfo)
        gradle.addListener(createBuildCompletionListener(dumpInfo))
    }

    private void startProfiling(JfrDumpInfo dumpInfo) {
        int counter = 0
        // wait until previous dump has been completed
        while (dumpInfo.isProfiling()) {
            System.out.println("Waiting for previous profiling to end...")
            Thread.sleep(500L)
            if (counter++ > 10) {
                break
            }

        }
        if (!dumpInfo.isProfiling()) {
            dumpInfo.markProfiling()
            // use profiling.jfc file if it exists
            File settingsFile = new File(dumpInfo.rootDir, "profiling.jfc")
            if (settingsFile.exists()) {
                System.out.println(jfrStart(dumpInfo.recordingName, settingsFile))
            } else {
                // copy profiling settings from classpath to temp file
                File settingsTempFile = File.createTempFile("profiling", ".jfc")
                settingsTempFile.deleteOnExit()
                settingsTempFile.withOutputStream { outputStream ->
                    getClass().getResourceAsStream("profiling.jfc").withStream { inputStream ->
                        outputStream << inputStream
                    }
                }
                System.out.println(jfrStart(dumpInfo.recordingName, settingsTempFile))
            }
        } else {
            System.out.println("Cannot start profiling since ${dumpInfo.dumpLockFile} exists.")
        }
    }

    @CompileStatic
    private static class JfrDumpInfo {
        File rootDir
        String recordingName
        File dumpLockFile
        StartParameter startParameter

        JfrDumpInfo(File rootDir, StartParameter startParameter) {
            this.rootDir = rootDir
            this.startParameter = startParameter
            recordingName = "${rootDir.name}-GradleProfiling"
            dumpLockFile = new File(rootDir, "${recordingName}.jfr.lock")
        }

        File createRecordingFile() {
            String prefix = "${recordingName}-${new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date())}"
            new File(rootDir, "${prefix}.info.txt").text = startParameter?.toString()
            new File(rootDir, "${prefix}.info.json").text = new groovy.json.JsonBuilder(startParameter).toPrettyString()
            new File(rootDir, "${prefix}.jfr").absoluteFile
        }

        boolean isProfiling() {
            dumpLockFile.exists()
        }

        void markProfiling() {
            dumpLockFile.text = 'profiling ' + startParameter
        }

        void clearStatus() {
            dumpLockFile.delete()
        }
    }
}