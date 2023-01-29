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
import org.apache.dubbo.annotation.util.FileUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dispatching annotation processor, which uses a file to locate handlers and invoke them.
 */
@SupportedAnnotationTypes("*")
public class DispatchingAnnotationProcessor extends AbstractProcessor {

    private static final Set<AnnotationProcessingHandler> handlers = loadHandlers();

    private static Set<AnnotationProcessingHandler> loadHandlers() {
        List<String> classNames = FileUtils.loadConfigurationFileInResources("handlers.cfg");
        Set<AnnotationProcessingHandler> tempReporters = new HashSet<>();

        for (String clsName : classNames) {
            try {
                Class<? extends AnnotationProcessingHandler> cls = (Class<? extends AnnotationProcessingHandler>) Class.forName(clsName);
                AnnotationProcessingHandler r = cls.getConstructor().newInstance();

                tempReporters.add(r);

            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return Collections.unmodifiableSet(tempReporters);
    }

    private AnnotationProcessorContext apContext;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        Permit.addOpens();
        super.init(processingEnv);

        apContext = AnnotationProcessorContext.fromProcessingEnvironment(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (AnnotationProcessingHandler i : handlers) {
            Set<Element> elements = new HashSet<>(16);

            for (Class<? extends Annotation> annotationClass : i.getAnnotationsToHandle()) {
                elements.addAll(roundEnv.getElementsAnnotatedWith(annotationClass));
            }

            i.process(elements, apContext);
        }

        return true;
    }
}
