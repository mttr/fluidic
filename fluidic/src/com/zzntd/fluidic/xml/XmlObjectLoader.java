package com.zzntd.fluidic.xml;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Composite;
import com.zzntd.fluidic.core.Priority;

public class XmlObjectLoader {
	private ObjectMap<String, ActorComposite> objectRegistry;
		
	public static class XmlLoader extends AsynchronousAssetLoader<Element, XmlObjectLoader.XmlLoader.XmlParameter> {
		private XmlReader xmlReader;
		private Element element;
		
		static public class XmlParameter extends AssetLoaderParameters<Element> {			
		}

		public XmlLoader(FileHandleResolver resolver) {
			super(resolver);
			xmlReader = new XmlReader();
		}

		@Override
		public void loadAsync(AssetManager manager, String fileName,
				FileHandle file, XmlParameter parameter) {
			try {
				element = xmlReader.parse(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Element loadSync(AssetManager manager, String fileName,
				FileHandle file, XmlParameter parameter) {
			return element;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Array<AssetDescriptor> getDependencies(String fileName,
				FileHandle file, XmlParameter parameter) {
			return null;
		}
		
	}
	
	public XmlObjectLoader() {
		AssetManager assets = Services.getAssetManager();
		
		assets.setLoader(Element.class, new XmlLoader(new InternalFileHandleResolver()));
		objectRegistry = new ObjectMap<String, ActorComposite>();
	}
	
	/**
	 * 
	 * @param name Name of object we're attempting to load. Searches for 
	 * name.xml in the assets/objects directory.
	 */
	public void loadObject(String name) {
		AssetManager assets = Services.getAssetManager();
		String path = "objects/" + name + ".xml";
		
		if (!objectRegistry.containsKey(name)) {
			assets.load(path, Element.class);
		}
		objectRegistry.put(path, new ActorComposite(null));
	}
	
	/**
	 * Creates an ActorComposite filled with components as defined in
	 * an xml file (assets/objects/...)
	 * 
	 * @param name Name of object we're attempting to load. Searches for 
	 * name.xml in the assets/objects directory.
	 */
	public ActorComposite getObject(String name) {
		AssetManager assets = Services.getAssetManager();
		assets.finishLoading(); // FIXME
		String path = "objects/" + name + ".xml";
		
		
		Element objectDef = assets.get(path);
		
		ActorComposite actor = loadFromObjectDef(objectDef, path);
		
		return actor;
	}
	
	/**
	 * Create Actors from all loaded object definitions, and store them
	 * in `destination`.
	 * 
	 * @param destination
	 */
	public void putObjectsIntoComposite(Composite destination) {
		AssetManager assets = Services.getAssetManager();
		
		for ( String path : objectRegistry.keys()) {
			Element objectDef = assets.get(path);
			ActorComposite actor = loadFromObjectDef(objectDef, path);
			actor.addTo(destination);
		}
	}
	
	/**
	 * This is mostly for non Gdx related unit testing.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public ActorComposite loadDirect(InputStream input) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element objectDef = xmlReader.parse(input);
		
		return loadFromObjectDef(objectDef, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ActorComposite loadFromObjectDef(Element objectDef, String path) {
		String prefix = objectDef.getAttribute("prefix");
		String s_priority = objectDef.getAttribute("priority", "MID");
		String name = objectDef.get("name");
		boolean defined = objectDef.getBoolean("defined", false);
		Priority priority = Priority.valueOf(s_priority);
		
		ActorComposite actor;
		
		if (!defined) {
			actor = new ActorComposite(priority);
		}
		else {
			try {
				Class compClass = ClassReflection.forName(prefix + name);
				actor = ClassReflection.newInstance(compClass);
				actor.setPriority(priority);
			} catch (ReflectionException e) {
				throw new RuntimeException(e);
			}
		}
		
		for (int i = 0; i < objectDef.getChildCount(); i++) {
			Element child = objectDef.getChild(i);
			
			String className = child.getName();
			
			try {
				Class compClass = ClassReflection.forName(prefix + className);
				
				XmlSerializable comp = ClassReflection.newInstance(compClass);
				comp.fromXml(child, actor);
			} catch (ReflectionException e) {
				throw new RuntimeException(e);
			}
		}
		actor.sort();
		
		return actor;
	}
}
