/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.deployer.resource.docker.registry;

import org.junit.Test;
import org.springframework.cloud.deployer.resource.docker.DockerResource;
import org.springframework.cloud.deployer.resource.docker.registry.model.DockerImageLabel;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link DockerRegistryInfo}.
 *
 * @author Chris Schaefer
 */
public class DockerRegistryInfoTests {
	/* This image should be updated with on out of the springcloud dockerhub
	 * org and updated with labels that are more representative to the metadata
	 * that is intended to be conveyed. Maybe create and image without labels
	 * and put under springcloud org as well just to keep things in a known state.
	 */
	private static final String IMAGE_WITH_LABELS = "chrisjs/labeltest:latest";
	private static final String IMAGE_WITHOUT_LABELS = "library/busybox:latest";
	private static final String INVALID_IMAGE_NAME = "myimage";

	@Test
	public void testImageWithLabels() {
		DockerResource dockerResource = new DockerResource(IMAGE_WITH_LABELS);

		DockerRegistryInfo dockerRegistryInfo = new DockerRegistryInfo(dockerResource);
		Map<String, String> imageLabels = dockerRegistryInfo.getImageLabels();

		assertNotNull("Image labels should not be null", imageLabels);
		assertTrue("Image should contain 2 labels", imageLabels.size() == 2);

		assertTrue("Expected key \"boot\" not found", imageLabels.containsKey("boot"));
		assertEquals("Expected value for key \"boot\" invalid", "2.0", imageLabels.get("boot"));

		assertTrue("Expected key \"another\" not found", imageLabels.containsKey("another"));
		assertEquals("Expected value for key \"another\" invalid", "added.later", imageLabels.get("another"));
	}

	@Test
	public void testImageWithoutLabels() {
		DockerResource dockerResource = new DockerResource(IMAGE_WITHOUT_LABELS);

		DockerRegistryInfo dockerRegistryInfo = new DockerRegistryInfo(dockerResource);
		Map<String, String> imageLabels = dockerRegistryInfo.getImageLabels();

		assertNotNull("Image labels should not be null", imageLabels);
		assertTrue("Image labels should be empty", imageLabels.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImageWithInvalidName() {
		DockerResource dockerResource = new DockerResource(INVALID_IMAGE_NAME);
		new DockerRegistryInfo(dockerResource);
		fail();
	}

	@Test
	public void testGetExistingImageLabel() {
		DockerResource dockerResource = new DockerResource(IMAGE_WITH_LABELS);

		DockerRegistryInfo dockerRegistryInfo = new DockerRegistryInfo(dockerResource);
		DockerImageLabel dockerImageLabel = dockerRegistryInfo.getImageLabel(DockerImageLabels.BOOT_VERSION.getLabelName());

		assertNotNull("Docker image label not found", dockerImageLabel);
		assertEquals("Invalid name for label", "boot", dockerImageLabel.getLabelName());
		assertEquals("Invalid value for label", "2.0", dockerImageLabel.getLabelValue());
	}

	@Test
	public void testGetNonexistentImageLabel() {
		DockerResource dockerResource = new DockerResource(IMAGE_WITH_LABELS);

		DockerRegistryInfo dockerRegistryInfo = new DockerRegistryInfo(dockerResource);
		DockerImageLabel dockerImageLabel = dockerRegistryInfo.getImageLabel("NonexistentLabel");

		assertNull("Nonexistent Docker image label should not be found", dockerImageLabel);
	}
}
