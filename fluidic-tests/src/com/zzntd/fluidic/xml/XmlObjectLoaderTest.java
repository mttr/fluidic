package com.zzntd.fluidic.xml;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.assets.AssetManager;
import com.zzntd.fluidic.DirectionalInputComponent;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.ActorCompositeTest;
import com.zzntd.fluidic.core.ActorCompositeTest.MockComponent;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Composite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.goop.GoopPhysicsComponent;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.services.CollisionManager;

public class XmlObjectLoaderTest {
	@SuppressWarnings("unchecked")
	private static <T> T getObject(ActorComposite actor,
			Class<T> compClass) {
		for (Component c : actor.getChildren()) {
			if (compClass.isInstance(c)) {
				return (T) c;
			}
		}
		return null;
	}
	
	@Before
	public void setUp() {
		AssetManager assetManager = new AssetManager();
		Services.provideAssetManager(assetManager);
		Services.provideCollisionManager(new CollisionManager());

	}
	
	public ActorComposite getObject(String fileName) {
		XmlObjectLoader xmlObjectLoader = new XmlObjectLoader();
		ActorComposite actor = null;
		
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			actor = xmlObjectLoader.loadDirect(fstream);
			fstream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return actor;
	}

	@Test
	public void testPhysicsComponentInitialized() {
		ActorComposite actor = getObject("TestObject.xml");
		PhysicsComponent physics = getObject(actor, PhysicsComponent.class);
		
		assertTrue(physics.isActive);
	}
	
	@Test
	public void testGoopPhysicsComponentInitialized() {
		ActorComposite actor = getObject("TestObject.xml");
		GoopPhysicsComponent physics = getObject(actor, GoopPhysicsComponent.class);
		
		assertTrue(physics.isActive);
	}
	
	@Test
	public void testInputComponentInitialized() {
		ActorComposite actor = getObject("TestObject.xml");
		DirectionalInputComponent input = getObject(actor, DirectionalInputComponent.class);
		
		assertTrue(input != null);
	}
	
	@Test
	public void testSort() {
		ActorComposite actorA = getObject("TestObject.xml");
		ActorComposite actorB = getObject("TestObject2.xml");
		
		MockComponent mockA = new ActorCompositeTest.MockComponent(Priority.HIGH);
		MockComponent mockB = new ActorCompositeTest.MockComponent(Priority.HIGH);
		
		actorA.clear();
		actorB.clear();
		
		mockA.addTo(actorA);
		mockB.addTo(actorB);
		
		Composite composite = new Composite(Priority.HIGH);
		
		actorB.addTo(composite);
		actorA.addTo(composite);
		
		composite.sort();
		composite.update(0);
		
		assertTrue(mockA.myUpdate < mockB.myUpdate);
	}
}
