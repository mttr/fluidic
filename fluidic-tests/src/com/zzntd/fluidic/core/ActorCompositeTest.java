package com.zzntd.fluidic.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActorCompositeTest {
	public static class MockComponent extends Component {
		public static int updates = 0;
		public int myUpdate;
		
		public MockComponent(Priority priority) {
			super(priority);
			myUpdate = 0;
		}

		@Override
		public void update(float dt) {
			myUpdate = updates;
			updates++;
		}		
	}

	@Test
	public void test() {
		MockComponent.updates = 0;
		MockComponent compA = new MockComponent(Priority.HIGH);
		MockComponent compB = new MockComponent(Priority.LOW);
		Composite composite = new Composite(null);
		
		compA.addTo(composite);
		compB.addTo(composite);
		
		composite.sort();
		composite.update(0);
		
		assertTrue(compA.myUpdate < compB.myUpdate);
	}

}
