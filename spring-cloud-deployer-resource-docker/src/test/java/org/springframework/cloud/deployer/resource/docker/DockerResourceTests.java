/*
 * Copyright 2016-2018 the original author or authors.
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

package org.springframework.cloud.deployer.resource.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Tests for the {@link DockerResource}.
 *
 * @author Thomas Risberg
 * @author Chris Schaefer
 */
public class DockerResourceTests {

	String image = "springcloud/hello-kube:latest";

	@Test
	public void testResource() throws IOException, URISyntaxException {
		DockerResource r = new DockerResource(image);
		assertEquals(image, r.getURI().getSchemeSpecificPart());
	}

	@Test
	public void testUri() throws IOException, URISyntaxException {
		DockerResource r = new DockerResource(URI.create(DockerResource.URI_SCHEME + ":" + image));
		assertEquals(image, r.getURI().getSchemeSpecificPart());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInvalidUri() throws IOException, URISyntaxException {
		new DockerResource(URI.create("http:" + image));
	}

	@Test
	public void testParseImageName() {
		DockerResource r = new DockerResource(image);
		assertEquals("springcloud/hello-kube", r.getImageName());
		assertEquals("latest", r.getImageTag());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testParseInvalidImageName() {
		DockerResource r = new DockerResource("springcloud/hello-kube");
		assertEquals("springcloud/hello-kube", r.getImageName());
		fail();
	}
}
