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
package com.intellij.lang.properties.references;

import com.intellij.lang.properties.*;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.application.AllIcons;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributes;
import consulo.document.util.TextRange;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.LookupElementPresentation;
import consulo.language.editor.completion.lookup.LookupElementRenderer;
import consulo.language.pom.PomService;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.content.ProjectRootManager;
import consulo.properties.localize.PropertiesLocalize;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.HashingStrategy;
import consulo.util.collection.Lists;
import consulo.util.collection.Sets;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author nik
 */
public abstract class PropertyReferenceBase implements PsiPolyVariantReference, EmptyResolveMessageProvider {
  private static final Logger LOG = Logger.getInstance(PropertyReferenceBase.class);
  private static final LookupElementRenderer<LookupElement> LOOKUP_ELEMENT_RENDERER = new LookupElementRenderer<LookupElement>() {
    @Override
    public void renderElement(LookupElement element, LookupElementPresentation presentation) {
      IProperty property = (IProperty)element.getObject();
      presentation.setIcon(AllIcons.Nodes.Property);
      String key = StringUtil.notNullize(property.getUnescapedKey());
      presentation.setItemText(key);

      PropertiesFile propertiesFile = property.getPropertiesFile();
      ResourceBundle resourceBundle = propertiesFile.getResourceBundle();
      String value = property.getValue();
      boolean hasBundle = resourceBundle != ResourceBundleImpl.NULL;
      if (hasBundle) {
        PropertiesFile defaultPropertiesFile = resourceBundle.getDefaultPropertiesFile(propertiesFile.getProject());
        IProperty defaultProperty = defaultPropertiesFile.findPropertyByKey(key);
        if (defaultProperty != null) {
          value = defaultProperty.getValue();
        }
      }

      if (hasBundle) {
        presentation.setTypeText(resourceBundle.getBaseName(), AllIcons.FileTypes.Properties);
      }

      TextAttributes attrs = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_VALUE);
      presentation.setTailText("=" + value, attrs.getForegroundColor());
    }
  };
  protected final String myKey;
  protected final PsiElement myElement;
  protected boolean mySoft;
  private final TextRange myTextRange;

  public PropertyReferenceBase(@Nonnull String key, final boolean soft, @Nonnull PsiElement element) {
    this(key, soft, element, ElementManipulators.getValueTextRange(element));
  }

  public PropertyReferenceBase(@Nonnull String key, final boolean soft, @Nonnull PsiElement element, TextRange range) {
    myKey = key;
    mySoft = soft;
    myElement = element;
    myTextRange = range;
  }

  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @Nonnull
  protected String getKeyText() {
    return myKey;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PropertyReferenceBase other = (PropertyReferenceBase)o;

    return getElement() == other.getElement() && getKeyText().equals(other.getKeyText());
  }

  public int hashCode() {
    return getKeyText().hashCode();
  }

  @Nonnull
  public PsiElement getElement() {
    return myElement;
  }

  public TextRange getRangeInElement() {
    return myTextRange;
  }

  @Nonnull
  public String getCanonicalText() {
    return myKey;
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
  {
    /*PsiElementFactory factory = JavaPsiFacade.getInstance(myElement.getProject()).getElementFactory();

    if (myElement instanceof PsiLiteralExpression) {
      PsiExpression newExpression = factory.createExpressionFromText("\"" + newElementName + "\"", myElement);
      return myElement.replace(newExpression);
    }
    else {*/
      ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(myElement);
      if (manipulator == null) {
        LOG.error("Cannot find manipulator for " + myElement + " of class " + myElement.getClass());
      }
      return manipulator.handleContentChange(myElement, getRangeInElement(), newElementName);
    /*}*/
  }

  public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
  {
    throw new IncorrectOperationException("not implemented");
  }

  public boolean isReferenceTo(PsiElement element) {
    for (ResolveResult result : multiResolve(false)) {
      final PsiElement el = result.getElement();
      if (el != null && el.isEquivalentTo(element)) return true;
    }
    return false;
  }

  protected void addKey(Object property, Set<Object> variants) {
    variants.add(property);
  }

  protected void addVariantsFromFile(final PropertiesFile propertiesFile, final Set<Object> variants) {
    if (propertiesFile == null) return;
    if (!ProjectRootManager.getInstance(myElement.getProject()).getFileIndex().isInContent(propertiesFile.getVirtualFile())) return;
    List<? extends IProperty> properties = propertiesFile.getProperties();
    for (IProperty property : properties) {
      addKey(property, variants);
    }
  }

  protected void setSoft(final boolean soft) {
    mySoft = soft;
  }

  public boolean isSoft() {
    return mySoft;
  }

  @Nonnull
  @Override
  public LocalizeValue buildUnresolvedMessage(@Nonnull String referenceText) {
    return PropertiesLocalize.unresolvedPropertyKey();
  }

  @Nonnull
  public ResolveResult[] multiResolve(final boolean incompleteCode) {
    final String key = getKeyText();

    List<IProperty> properties;
    final List<PropertiesFile> propertiesFiles = getPropertiesFiles();
    if (propertiesFiles == null) {
      properties = PropertiesUtil.findPropertiesByKey(getElement().getProject(), key);
    }
    else {
      properties = new ArrayList<IProperty>();
      for (PropertiesFile propertiesFile : propertiesFiles) {
        properties.addAll(propertiesFile.findPropertiesByKey(key));
      }
    }
    // put default properties file first
    Lists.quickSort(properties, new Comparator<IProperty>() {
      public int compare(final IProperty o1, final IProperty o2) {
        String name1 = o1.getPropertiesFile().getName();
        String name2 = o2.getPropertiesFile().getName();
        return Comparing.compare(name1, name2);
      }
    });
    return getResolveResults(properties);
  }

  protected static ResolveResult[] getResolveResults(List<IProperty> properties) {
    if (properties.isEmpty()) return ResolveResult.EMPTY_ARRAY;

    final ResolveResult[] results = new ResolveResult[properties.size()];
    for (int i = 0; i < properties.size(); i++) {
      IProperty property = properties.get(i);
      results[i] = new PsiElementResolveResult(property instanceof PsiElement ? (PsiElement)property : PomService.convertToPsi(
                        (PsiTarget)property));
    }
    return results;
  }

  @Nullable
  protected abstract List<PropertiesFile> getPropertiesFiles();

  @Nonnull
  public Object[] getVariants() {
    final Set<Object> variants = Sets.newHashSet(new HashingStrategy<Object>() {
      public int hashCode(final Object object) {
        if (object instanceof IProperty) {
          final String key = ((IProperty)object).getKey();
          return key == null ? 0 : key.hashCode();
        }
        else {
          return 0;
        }
      }

      public boolean equals(final Object o1, final Object o2) {
        return o1 instanceof IProperty && o2 instanceof IProperty &&
               Comparing.equal(((IProperty)o1).getKey(), ((IProperty)o2).getKey(), true);
      }
    });
    List<PropertiesFile> propertiesFileList = getPropertiesFiles();
    if (propertiesFileList == null) {
      PropertiesReferenceManager.getInstance(myElement.getProject()).processAllPropertiesFiles(new PropertiesFileProcessor() {
        @Override
        public boolean process(String baseName, PropertiesFile propertiesFile) {
          addVariantsFromFile(propertiesFile, variants);
          return true;
        }
      });
    }
    else {
      for (PropertiesFile propFile : propertiesFileList) {
        addVariantsFromFile(propFile, variants);
      }
    }
    return getVariants(variants);
  }

  protected static Object[] getVariants(Set<Object> variants) {
    return ContainerUtil.mapNotNull(variants, new Function<Object, Object>() {
      @Override
      public LookupElement apply(Object o) {
        if (o instanceof String) return LookupElementBuilder.create((String)o).withIcon(AllIcons.Nodes.Property);
        IProperty property = (IProperty)o;
        String key = property.getKey();
        if (key == null) return null;

        return LookupElementBuilder.create(property, key).withRenderer(LOOKUP_ELEMENT_RENDERER);
      }
    }).toArray();
  }
}
