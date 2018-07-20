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

package org.springframework.cloud.deployer.resource.docker.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Attributes mapped from the "history" element of the image manifest.
 *
 * @author Chris Schaefer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerManifest {
	@JsonProperty("history")
	private List<DockerHistory> history;

	public List<DockerHistory> getDockerHistory() {
		if (history == null) {
			return Collections.emptyList();
		}

		return history;
	}

	public void setDockerHistory(List<DockerHistory> history) {
		this.history = history;
	}
}
