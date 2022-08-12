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
package com.intellij.lang.properties.parsing;

import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.Property;
import com.intellij.lang.properties.psi.PropertyKeyIndex;
import com.intellij.lang.properties.psi.PropertyStub;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import com.intellij.lang.properties.psi.impl.PropertyStubImpl;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.*;

import javax.annotation.Nonnull;
import java.io.IOException;

public class PropertyStubElementType extends IStubElementType<PropertyStub, Property> {
  public PropertyStubElementType() {
    super("PROPERTY", PropertiesLanguage.INSTANCE);
  }

  public Property createPsi(@Nonnull final PropertyStub stub) {
    return new PropertyImpl(stub, this);
  }

  public PropertyStub createStub(@Nonnull final Property psi, final StubElement parentStub) {
    return new PropertyStubImpl(parentStub, psi.getKey());
  }

  @Nonnull
  public String getExternalId() {
    return "properties.PROPERTY";
  }

  public void serialize(@Nonnull final PropertyStub stub, @Nonnull final StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getKey());
  }

  @Nonnull
  public PropertyStub deserialize(@Nonnull final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    final StringRef ref = dataStream.readName();
    return new PropertyStubImpl(parentStub, ref.getString());
  }

  public void indexStub(@Nonnull final PropertyStub stub, @Nonnull final IndexSink sink) {
    sink.occurrence(PropertyKeyIndex.KEY, PropertyImpl.unescape(stub.getKey()));
  }
}