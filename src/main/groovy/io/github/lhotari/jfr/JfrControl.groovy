package io.github.lhotari.jfr

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import javax.management.DynamicMBean
import javax.management.MBeanException
import javax.management.MBeanOperationInfo
import javax.management.ReflectionException
import java.lang.reflect.Array

@CompileStatic
class JfrControl {
    // in Java: {String[].getClass().getName()}
    private static final String[] STRING_ARRAY_ARGUMENT = [Array.newInstance(String, 0).getClass().getName()] as String[]


    static String jfrDump(String recordingName, File recordingFile) throws ReflectionException, MBeanException {
        return callDiagnosticsMethod("jfrDump", "name=" + recordingName, "filename=" + recordingFile.getAbsolutePath())
    }

    static String jfrStart(String recordingName, File settingsFile) throws ReflectionException, MBeanException {
        return callDiagnosticsMethod("jfrStart", "name=" + recordingName, "settings=" + settingsFile.getAbsolutePath())
    }

    static String jfrStart(String recordingName) throws ReflectionException, MBeanException {
        return callDiagnosticsMethod("jfrStart", "name=" + recordingName, "settings=profile")
    }

    static String jfrStop(String recordingName, File recordingFile) throws ReflectionException, MBeanException {
        return callDiagnosticsMethod("jfrStop", "name=" + recordingName, "filename=" + recordingFile.getAbsolutePath())
    }

    static String callDiagnosticsMethod(String actionName, String... args) throws MBeanException, ReflectionException {
        return (String) diagnosticCommandMBean.invoke(actionName, [args] as Object[], STRING_ARRAY_ARGUMENT)
    }

    static boolean jfrEnabled() {
        DynamicMBean dcmd = getDiagnosticCommandMBean()
        dcmd.getMBeanInfo().getOperations().find { MBeanOperationInfo operationInfo -> operationInfo.name == 'jfrStart' }
    }

    @CompileDynamic
    private static DynamicMBean getDiagnosticCommandMBean() {
        return sun.management.ManagementFactoryHelper.getDiagnosticCommandMBean()
    }
}
