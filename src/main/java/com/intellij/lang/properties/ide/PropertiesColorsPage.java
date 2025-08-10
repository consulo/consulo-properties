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
package com.intellij.lang.properties.ide;

import com.intellij.lang.properties.PropertiesHighlighter;
import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import consulo.properties.localize.PropertiesLocalize;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class PropertiesColorsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[PropertiesHighlighter.DISPLAY_NAMES.size()];
    TextAttributesKey[] keys = PropertiesHighlighter.DISPLAY_NAMES.keySet().toArray(new TextAttributesKey[0]);
    for (int i = 0; i < keys.length; i++) {
      TextAttributesKey key = keys[i];
      String name = PropertiesHighlighter.DISPLAY_NAMES.get(key).getFirst();
      ATTRS[i] = new AttributesDescriptor(name, key);
    }
  }

  @Nonnull
  public LocalizeValue getDisplayName() {
    return PropertiesLocalize.propertiesDisplayName();
  }

  @Nonnull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @Nonnull
  public SyntaxHighlighter getHighlighter() {
    return new PropertiesHighlighter();
  }

  @Nonnull
  public String getDemoText() {
    return "# Comment on keys and values\n" +
           "key1=value1\n" +
           "! other values:\n" +
           "a\\=\\fb : x\\ty\\n\\x\\uzzzz\n"
      ;
  }
}
