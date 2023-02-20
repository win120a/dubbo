/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.annotation;

import org.apache.dubbo.annotation.permit.Permit;

import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * The Context Object of Annotation Processor, which stores objects related to javac.
 */
public class AnnotationProcessorContext {
    private JavacProcessingEnvironment javacProcessingEnvironment;
    private JavacTrees javacTrees;
    private TreeMaker treeMaker;
    private Names names;
    private Context javacContext;
    private Trees trees;

    private JavacFiler javacFiler;

    private JavacFileManager javacFileManager;

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

        apContext.javacProcessingEnvironment = jcProcessingEnvironment;

        apContext.javacContext = context;
        apContext.javacTrees = JavacTrees.instance(jcProcessingEnvironment);
        apContext.treeMaker = TreeMaker.instance(context);
        apContext.names = Names.instance(context);

        apContext.trees = Trees.instance(jcProcessingEnvironment);

        Object tempFiler = jcProcessingEnvironment.getFiler();
        apContext.javacFiler = (JavacFiler) (tempFiler.getClass() == JavacFiler.class ?
            tempFiler : jbUnwrap(JavacFiler.class, tempFiler));

        Object tempFM = context.get(JavaFileManager.class);
        apContext.javacFileManager = unwrapFileManager(tempFM);

        return apContext;
    }

    private static JavacFileManager unwrapFileManager(Object wrappedFileManager) {
        try {
            Class wrappedJFMClass = Class.forName("com.sun.tools.javac.api.ClientCodeWrapper$WrappedJavaFileManager");
            Field ideaWrappedJFMField = Permit.getField(wrappedJFMClass, "clientJavaFileManager");

            Object tempJFM = ideaWrappedJFMField.get(wrappedFileManager);

            return (JavacFileManager) (tempJFM.getClass() == JavacFileManager.class ?
                tempJFM : jbUnwrap(JavacFileManager.class, tempJFM));

        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
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

    public JavacProcessingEnvironment getJavacProcessingEnvironment() {
        return javacProcessingEnvironment;
    }

    public JavacFiler getJavacFiler() {
        return javacFiler;
    }

    public JavacFileManager getJavacFileManager() {
        return javacFileManager;
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
