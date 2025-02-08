// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.properties;

import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

/**
 * @author Anna Bulenkova
 */
public final class EmptyResourceBundle {
    private EmptyResourceBundle() {
    }

    private static class Holder {
        public static final ResourceBundle NULL = new ResourceBundle() {
            @Override
            @Nonnull
            public List<PropertiesFile> getPropertiesFiles(final Project project) {
                return Collections.emptyList();
            }

            @Override
            @Nonnull
            public PropertiesFile getDefaultPropertiesFile(final Project project) {
                throw new IllegalStateException();
            }

            @Override
            @Nonnull
            public String getBaseName() {
                return "";
            }

            @Override
            @Nonnull
            public VirtualFile getBaseDirectory() {
                throw new IllegalStateException();
            }
        };
    }

    public static ResourceBundle getInstance() {
        return Holder.NULL;
    }
}
