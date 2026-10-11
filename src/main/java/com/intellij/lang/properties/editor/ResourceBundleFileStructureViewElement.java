/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.image.Image;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexey
 */
public class ResourceBundleFileStructureViewElement implements StructureViewTreeElement {
    private final Project myProject;
    private final ResourceBundle myResourceBundle;

    public ResourceBundleFileStructureViewElement(Project project, ResourceBundle resourceBundle) {
        myProject = project;
        myResourceBundle = resourceBundle;
    }

    @Override
    public ResourceBundle getValue() {
        return myResourceBundle;
    }

    @Override
    @RequiredReadAction
    public StructureViewTreeElement[] getChildren() {
        List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(myProject);
        Map<String, IProperty> propertyNames = new LinkedHashMap<>();
        for (PropertiesFile propertiesFile : propertiesFiles) {
            List<IProperty> properties = propertiesFile.getProperties();
            for (IProperty property : properties) {
                String name = property.getUnescapedKey();
                if (name != null && !propertyNames.containsKey(name)) {
                    propertyNames.put(name, property);
                }
            }
        }
        List<StructureViewTreeElement> result = new ArrayList<>(propertyNames.size());
        for (String property : propertyNames.keySet()) {
            //result.add(new PropertiesStructureViewElement(property));
            result.add(new ResourceBundlePropertyStructureViewElement(myProject, myResourceBundle, property));
        }
        return result.toArray(new StructureViewTreeElement[result.size()]);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.of(myResourceBundle.getBaseName());
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Image getIcon() {
                return PlatformIconGroup.filetypesProperties();
            }
        };
    }

    @Override
    public void navigate(boolean requestFocus) {
    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }
}
