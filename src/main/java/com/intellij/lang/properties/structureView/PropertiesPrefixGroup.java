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
package com.intellij.lang.properties.structureView;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.editor.ResourceBundlePropertyStructureViewElement;
import consulo.fileEditor.structureView.tree.Group;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cdr
 */
public class PropertiesPrefixGroup implements Group {
    private final Collection<TreeElement> myProperties;
    private final String myPrefix;
    private final String myPresentableName;
    private final String mySeparator;

    public PropertiesPrefixGroup(Collection<TreeElement> properties, String prefix, String presentableName, String separator) {
        myProperties = properties;
        myPrefix = prefix;
        myPresentableName = presentableName;
        mySeparator = separator;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.of(myPresentableName);
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Image getIcon() {
                return PlatformIconGroup.nodesTag();
            }
        };
    }

    @Override
    public Collection<TreeElement> getChildren() {
        Collection<TreeElement> result = new ArrayList<>();
        List<String> prefixWords = StringUtil.split(myPrefix, mySeparator);
        for (TreeElement treeElement : myProperties) {
            String key;
            if (treeElement instanceof PropertiesStructureViewElement propertiesElement) {
                IProperty property = propertiesElement.getValue();

                key = property.getUnescapedKey();
            }
            else if (treeElement instanceof ResourceBundlePropertyStructureViewElement rbpSve) {
                key = rbpSve.getValue();
            }
            else {
                continue;
            }

            if (key == null || key.equals(myPrefix)) {
                continue;
            }
            List<String> keyWords = StringUtil.split(key, mySeparator);
            boolean startsWith = prefixWords.size() < keyWords.size();
            if (startsWith) {
                for (int i = 0; i < prefixWords.size(); i++) {
                    String prefixWord = prefixWords.get(i);
                    String keyWord = keyWords.get(i);
                    if (!Comparing.strEqual(keyWord, prefixWord)) {
                        startsWith = false;
                        break;
                    }
                }
            }
            if (startsWith) {
                result.add(treeElement);
                String presentableName = key.substring(myPrefix.length());
                presentableName = StringUtil.trimStart(presentableName, mySeparator);
                if (treeElement instanceof PropertiesStructureViewElement pSve) {
                    pSve.setPresentableName(presentableName);
                }
                if (treeElement instanceof ResourceBundlePropertyStructureViewElement rbpSve) {
                    rbpSve.setPresentableName(presentableName);
                }
            }
        }
        return result;
    }

    public String getPrefix() {
        return myPrefix;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PropertiesPrefixGroup that = (PropertiesPrefixGroup) o;

        return myPrefix.equals(that.myPrefix);
    }

    @Override
    public int hashCode() {
        return myPrefix.hashCode();
    }
}
