package com.intellij.lang.properties.xml;

import consulo.annotation.component.ExtensionImpl;
import consulo.index.io.DataIndexer;
import consulo.index.io.EnumeratorStringDescriptor;
import consulo.index.io.ID;
import consulo.index.io.KeyDescriptor;
import consulo.index.io.data.DataExternalizer;
import consulo.index.io.data.IOUtil;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.FileBasedIndexExtension;
import consulo.language.psi.stub.FileContent;
import consulo.project.Project;
import consulo.util.io.Readers;
import consulo.util.io.StreamUtil;
import consulo.util.lang.Comparing;
import consulo.util.xml.fastReader.NanoXmlUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.ide.highlighter.XmlFileType;
import jakarta.annotation.Nullable;
import net.n3.nanoxml.StdXMLReader;

import jakarta.annotation.Nonnull;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Avdeev
 *         Date: 7/25/11
 */
@ExtensionImpl
public class XmlPropertiesIndex extends FileBasedIndexExtension<XmlPropertiesIndex.Key, String> implements FileBasedIndex.InputFilter,
		DataIndexer<XmlPropertiesIndex.Key, String, FileContent>, KeyDescriptor<XmlPropertiesIndex.Key>
{

	public final static Key MARKER_KEY = new Key();
	public static final ID<Key, String> NAME = ID.create("xmlProperties");

	private static final EnumeratorStringDescriptor ENUMERATOR_STRING_DESCRIPTOR = new EnumeratorStringDescriptor();

	@Nonnull
	@Override
	public ID<Key, String> getName()
	{
		return NAME;
	}

	@Nonnull
	@Override
	public DataIndexer<Key, String, FileContent> getIndexer()
	{
		return this;
	}

	@Override
	public KeyDescriptor<Key> getKeyDescriptor()
	{
		return this;
	}

	@Override
	public DataExternalizer<String> getValueExternalizer()
	{
		return ENUMERATOR_STRING_DESCRIPTOR;
	}

	@Override
	public FileBasedIndex.InputFilter getInputFilter()
	{
		return this;
	}

	@Override
	public boolean dependsOnFileContent()
	{
		return true;
	}

	@Override
	public int getVersion()
	{
		return 1;
	}

	@Override
	public boolean acceptInput(Project project, VirtualFile file)
	{
		return XmlFileType.INSTANCE == file.getFileType();
	}

	@Nonnull
	@Override
	public Map<Key, String> map(FileContent inputData)
	{
		MyIXMLBuilderAdapter builder = parse(inputData.getContentAsText(), false);
		if(builder == null)
		{
			return Collections.emptyMap();
		}
		HashMap<Key, String> map = builder.myMap;
		if(builder.accepted)
		{
			map.put(MARKER_KEY, "");
		}
		return map;
	}

	static boolean isAccepted(CharSequence bytes)
	{
		MyIXMLBuilderAdapter builder = parse(bytes, true);
		return builder != null && builder.accepted;
	}

	@Nullable
	private static MyIXMLBuilderAdapter parse(CharSequence text, boolean stopIfAccepted)
	{
		StdXMLReader reader = new StdXMLReader(Readers.readerFromCharSequence(text))
		{
			@Override
			public Reader openStream(String publicID, String systemID) throws IOException
			{
				if(!"http://java.sun.com/dtd/properties.dtd".equals(systemID))
				{
					throw new IOException();
				}
				return new StringReader(" ");
			}
		};
		MyIXMLBuilderAdapter builder = new MyIXMLBuilderAdapter(stopIfAccepted);
		NanoXmlUtil.parse(reader, builder);
		return builder;
	}

	@Override
	public void save(DataOutput out, Key value) throws IOException
	{
		out.writeBoolean(value.isMarker);
		if(value.key != null)
		{
			IOUtil.writeUTF(out, value.key);
		}
	}

	@Override
	public Key read(DataInput in) throws IOException
	{
		boolean isMarker = in.readBoolean();
		return isMarker ? MARKER_KEY : new Key(IOUtil.readUTF(in));
	}

	@Override
	public int hashCode(Key value)
	{
		return value.hashCode();
	}

	@Override
	public boolean equals(Key val1, Key val2)
	{
		return val1.isMarker == val2.isMarker && Comparing.equal(val1.key, val2.key);
	}

	public static class Key
	{
		final boolean isMarker;
		final String key;

		public Key(String key)
		{
			this.key = key;
			isMarker = false;
		}

		public Key()
		{
			isMarker = true;
			key = null;
		}

		@Override
		public int hashCode()
		{
			return isMarker ? 0 : key.hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || getClass() != o.getClass())
			{
				return false;
			}

			Key key1 = (Key) o;

			if(isMarker != key1.isMarker)
			{
				return false;
			}
			if(key != null ? !key.equals(key1.key) : key1.key != null)
			{
				return false;
			}

			return true;
		}
	}

	private static class MyIXMLBuilderAdapter extends NanoXmlUtil.IXMLBuilderAdapter
	{

		boolean accepted;
		boolean insideEntry;
		String key;
		private final HashMap<Key, String> myMap = new HashMap<Key, String>();
		private final boolean myStopIfAccepted;

		public MyIXMLBuilderAdapter(boolean stopIfAccepted)
		{
			myStopIfAccepted = stopIfAccepted;
		}

		@Override
		public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception
		{
			if(!accepted)
			{
				if("properties".equals(name))
				{
					accepted = true;
				}
				else
				{
					throw NanoXmlUtil.ParserStoppedXmlException.INSTANCE;
				}
			}
			else
			{
				insideEntry = "entry".equals(name);
			}
			if(myStopIfAccepted)
			{
				throw NanoXmlUtil.ParserStoppedXmlException.INSTANCE;
			}
		}

		@Override
		public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception
		{
			if(insideEntry && "key".equals(key))
			{
				this.key = value;
			}
		}

		@Override
		public void addPCData(Reader reader, String systemID, int lineNr) throws Exception
		{
			if(insideEntry && key != null)
			{
				String value = StreamUtil.readTextFrom(reader);
				myMap.put(new Key(key), value);
			}
		}
	}
}
