package com.daytrip.compiler;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

public class SunriseCompilerPlugin implements Plugin {
    @Override
    public String getName() {
        return "SunriseCompilerPlugin";
    }

    @Override
    public void init(JavacTask javacTask, String... strings) {
        Context context = ((BasicJavacTask) javacTask).getContext();
        Log.instance(context).printRawLines(Log.WriterKind.NOTICE, "Hello from " + getName());
        /*javacTask.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent taskEvent) {

            }

            @Override
            public void finished(TaskEvent taskEvent) {
                if(taskEvent.getKind() == TaskEvent.Kind.PARSE) return;
                taskEvent.getCompilationUnit().accept(new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree methodTree, Void unused) {
                        System.out.println("A Method Was Visited");
                        return super.visitMethod(methodTree, unused);
                    }
                }, null);
            }
        });*/
    }
}
