/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.lang.properties;

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.impl.PropertiesPsiCompletionUtil;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.lang.properties.references.PropertyReferenceBase;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributes;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.LookupElementPresentation;
import consulo.language.editor.completion.lookup.LookupElementRenderer;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiReference;
import consulo.language.util.ProcessingContext;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * @author peter
 */
@ExtensionImpl(id = "propertiesCompletion", order = "before javaClassReference")
public class PropertiesCompletionContributor extends CompletionContributor {
    public PropertiesCompletionContributor() {
        extend(null, PlatformPatterns.psiElement(), new CompletionProvider() {
            @RequiredReadAction
            @Override
            public void addCompletions(@NotNull CompletionParameters parameters,
                                       @NotNull ProcessingContext context,
                                       @NotNull CompletionResultSet result) {
                doAdd(parameters, result);
            }
        });
    }

    @RequiredReadAction
    private static void doAdd(CompletionParameters parameters, final CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();
        PsiElement gParent = parent != null ? parent.getParent() : null;
        PsiReference[] references = parent == null ? position.getReferences() : ArrayUtil.mergeArrays(position.getReferences(), parent.getReferences());
        if (gParent instanceof PsiLanguageInjectionHost && references.length == 0) {
            //kotlin
            PsiReference[] gParentReferences = gParent.getReferences();
            if (gParentReferences.length > 0) {
                references = ArrayUtil.mergeArrays(references, gParentReferences);
            }
        }
        PropertyReference propertyReference = ContainerUtil.findInstance(references, PropertyReference.class);
        if (propertyReference != null && !hasMoreImportantReference(references, propertyReference)) {
            final int startOffset = parameters.getOffset();
            PsiElement element = propertyReference.getElement();
            final int offsetInElement = startOffset - element.getTextRange().getStartOffset();
            TextRange range = propertyReference.getRangeInElement();
            if (offsetInElement >= range.getStartOffset()) {
                final String prefix = element.getText().substring(range.getStartOffset(), offsetInElement);

                LookupElement[] variants = getVariants(propertyReference);
                result.withPrefixMatcher(prefix).addAllElements(Arrays.asList(variants));
            }
        }
    }

    @RequiredReadAction
    public static boolean hasMoreImportantReference(PsiReference @NotNull [] references, @NotNull PropertyReference propertyReference) {
        return propertyReference.isSoft() && ContainerUtil.or(references, reference -> !reference.isSoft());
    }

    public static final LookupElementRenderer<LookupElement> LOOKUP_ELEMENT_RENDERER = new LookupElementRenderer<>() {
        @Override
        public void renderElement(LookupElement element, LookupElementPresentation presentation) {
            IProperty property = (IProperty) element.getObject();
            presentation.setIcon(PlatformIconGroup.nodesProperty());
            String key = StringUtil.notNullize(property.getUnescapedKey());
            presentation.setItemText(key);

            PropertiesFile propertiesFile = property.getPropertiesFile();
            ResourceBundle resourceBundle = propertiesFile.getResourceBundle();
            String value = property.getValue();
            boolean hasBundle = resourceBundle != EmptyResourceBundle.getInstance();
            if (hasBundle) {
                PropertiesFile defaultPropertiesFile = resourceBundle.getDefaultPropertiesFile(propertiesFile.getProject());
                if (defaultPropertiesFile.getContainingFile() != propertiesFile.getContainingFile()) {
                    IProperty defaultProperty = defaultPropertiesFile.findPropertyByKey(key);
                    if (defaultProperty != null) {
                        value = defaultProperty.getValue();
                    }
                }
            }

            if (hasBundle) {
                presentation.setTypeText(resourceBundle.getBaseName(), AllIcons.FileTypes.Properties);
            }

            TextAttributes attrs = EditorColorsManager.getInstance().getGlobalScheme()
                .getAttributes(PropertiesHighlighter.PROPERTY_VALUE);
            presentation.setTailText("=" + value, attrs.getForegroundColor());
        }
    };

    @NotNull
    public static LookupElement[] getVariants(final PropertyReferenceBase propertyReference) {
        final Set<Object> variants = PropertiesPsiCompletionUtil.getPropertiesKeys(propertyReference);
        return getVariants(variants);
    }

    public static LookupElement[] getVariants(Set<Object> variants) {
        return variants.stream().map(o -> o instanceof String
                ? LookupElementBuilder.create((String) o).withIcon(PlatformIconGroup.nodesProperty())
                : createVariant((IProperty) o))
            .filter(Objects::nonNull).toArray(LookupElement[]::new);
    }

    @Nullable
    public static LookupElement createVariant(IProperty property) {
        String key = property.getKey();
        return key == null ? null : LookupElementBuilder.create(property, key).withRenderer(LOOKUP_ELEMENT_RENDERER);
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        if (context.getFile() instanceof PropertiesFile) {
            context.setDummyIdentifier(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return Language.ANY;
    }
}
