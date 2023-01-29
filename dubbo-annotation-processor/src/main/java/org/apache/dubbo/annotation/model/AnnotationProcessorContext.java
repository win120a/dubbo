package org.apache.dubbo.annotation.model;

import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * The Context Object of Annotation Processor, which stores objects related to javac.
 */
public class AnnotationProcessorContext {
    private JavacTrees javacTrees;
    private TreeMaker treeMaker;
    private Names names;
    private Context javacContext;
    private Trees trees;

    private AnnotationProcessorContext() { }

    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
        }

        return unwrapped != null ? unwrapped : wrapper;
    }

    public static AnnotationProcessorContext fromProcessingEnvironment(ProcessingEnvironment processingEnv) {
        AnnotationProcessorContext apContext = new AnnotationProcessorContext();

        Object procEnvToUnwrap = processingEnv.getClass() == JavacProcessingEnvironment.class ?
            processingEnv : jbUnwrap(JavacProcessingEnvironment.class, processingEnv);

        JavacProcessingEnvironment jcProcessingEnvironment = (JavacProcessingEnvironment) procEnvToUnwrap;

        Context context = jcProcessingEnvironment.getContext();

        apContext.javacContext = context;
        apContext.javacTrees = JavacTrees.instance(jcProcessingEnvironment);
        apContext.treeMaker = TreeMaker.instance(context);
        apContext.names = Names.instance(context);

        apContext.trees = Trees.instance(jcProcessingEnvironment);

        return apContext;
    }

    // Auto-generated methods.

    public JavacTrees getJavacTrees() {
        return javacTrees;
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public Names getNames() {
        return names;
    }

    public Context getJavacContext() {
        return javacContext;
    }

    public Trees getTrees() {
        return trees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationProcessorContext context = (AnnotationProcessorContext) o;

        if (!Objects.equals(javacTrees, context.javacTrees)) return false;
        if (!Objects.equals(treeMaker, context.treeMaker)) return false;
        if (!Objects.equals(names, context.names)) return false;
        if (!Objects.equals(javacContext, context.javacContext))
            return false;
        return Objects.equals(trees, context.trees);
    }

    @Override
    public int hashCode() {
        int result = javacTrees != null ? javacTrees.hashCode() : 0;
        result = 31 * result + (treeMaker != null ? treeMaker.hashCode() : 0);
        result = 31 * result + (names != null ? names.hashCode() : 0);
        result = 31 * result + (javacContext != null ? javacContext.hashCode() : 0);
        result = 31 * result + (trees != null ? trees.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnnotationProcessorContext{" +
            "javacTrees=" + javacTrees +
            ", treeMaker=" + treeMaker +
            ", names=" + names +
            ", javacContext=" + javacContext +
            ", trees=" + trees +
            '}';
    }
}
