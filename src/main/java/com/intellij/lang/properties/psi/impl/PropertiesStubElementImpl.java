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

/*
 * @author max
 */
package com.intellij.lang.properties.psi.impl;

import com.intellij.lang.properties.PropertiesLanguage;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import jakarta.annotation.Nonnull;

public class PropertiesStubElementImpl <T extends StubElement> extends StubBasedPsiElementBase<T> {
  public PropertiesStubElementImpl(final T stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PropertiesStubElementImpl(final ASTNode node) {
    super(node);
  }

  @Nonnull
  public Language getLanguage() {
    return PropertiesLanguage.INSTANCE;
  }
}