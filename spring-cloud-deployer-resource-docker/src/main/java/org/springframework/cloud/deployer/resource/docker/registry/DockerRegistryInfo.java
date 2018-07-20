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

import org.springframework.cloud.deployer.resource.docker.DockerResource;
import org.springframework.cloud.deployer.resource.docker.registry.model.DockerAuth;
import org.springframework.cloud.deployer.resource.docker.registry.model.DockerImageLabel;
import org.springframework.cloud.deployer.resource.docker.registry.model.DockerManifest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides operations to query the Docker Repository residing on docker.io for
 * information about the provided {@link DockerResource}. An authentication token
 * will be obtained at creation for use in subsequent calls.
 *
 * @author Chris Schaefer
 */
public class DockerRegistryInfo {
	private static final String DOCKER_REGISTRY_AUTH_URL_PREFIX = "https://auth.docker.io/token?scope=repository:";
	private static final String DOCKER_REGISTRY_AUTH_URL_SUFFIX = ":pull&service=registry.docker.io";
	private static final String DOCKER_REGISTRY_AUTH_TYPE = "Bearer";
	private static final String DOCKER_REGISTRY_URL = "https://registry-1.docker.io/v2/";
	private static final String DOCKER_REGISTRY_MANIFEST_RESOURCE_PATH = "/manifests/";
	private static final String APPLICATION_MEDIA_TYPE = "application";
	private static final String DOCKER_MANIFEST_MIME_PREFIX = "vnd.docker.distribution.manifest";
	private static final String DOCKER_MANIFEST_ACCEPT_HEADER = APPLICATION_MEDIA_TYPE + "/" + DOCKER_MANIFEST_MIME_PREFIX + ".v1+json";
	private static final String APPLICATION_MEDIA_SUBTYPE_DOCKER_MANIFEST = DOCKER_MANIFEST_MIME_PREFIX + ".v1+prettyjws";

	private DockerAuth dockerAuth;
	private RestTemplate restTemplate;
	private DockerResource dockerResource;

	public DockerRegistryInfo(DockerResource dockerResource) {
		this.dockerResource = dockerResource;
		this.restTemplate = configureRestTemplate();
		this.dockerAuth = getDockerAuth();
	}

	/**
	 * Obtains all LABEL attributes that are stored in the container manifest.
	 *
	 * @return {@link Map} representing all found image LABEL's
	 */
	public Map<String, String> getImageLabels() {
		return getDockerImageManifest().getDockerHistory().get(0).getDockerContainerConfig().getLabels();
	}

	/**
	 * Obtains a specific image label if found in the container manifest.
	 *
	 * @param imageLabel the image label to obtain
	 * @return a {@link DockerImageLabel} representing the LABEL, null if not found
	 */
	public DockerImageLabel getImageLabel(String imageLabel) {
		Map<String, String> imageLabels = getImageLabels();

		if (imageLabels.containsKey(imageLabel)) {
			return new DockerImageLabel(imageLabel, imageLabels.get(imageLabel));
		}

		return null;
	}

	private RestTemplate configureRestTemplate() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new DockerRegistryJsonHttpMessageConverter());

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setMessageConverters(messageConverters);

		return restTemplate;
	}

	private DockerAuth getDockerAuth() {
		return restTemplate.getForObject(getDockerAuthUrl(), DockerAuth.class);
	}

	private String getDockerAuthUrl() {
		return DOCKER_REGISTRY_AUTH_URL_PREFIX + dockerResource.getImageName() + DOCKER_REGISTRY_AUTH_URL_SUFFIX;
	}

	private DockerManifest getDockerImageManifest() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, DOCKER_MANIFEST_ACCEPT_HEADER);
		headers.add(HttpHeaders.AUTHORIZATION, DOCKER_REGISTRY_AUTH_TYPE + " " + dockerAuth.getToken());
		HttpEntity httpEntity = new HttpEntity(headers);

		String manifestEndpointUrl = getDockerManifestEndpointUrl();
		ResponseEntity manifest = restTemplate.exchange(manifestEndpointUrl, HttpMethod.GET, httpEntity,
				DockerManifest.class);

		return (DockerManifest) manifest.getBody();
	}

	private String getDockerManifestEndpointUrl() {
		return DOCKER_REGISTRY_URL + dockerResource.getImageName() + DOCKER_REGISTRY_MANIFEST_RESOURCE_PATH
				+ dockerResource.getImageTag();
	}

	class DockerRegistryJsonHttpMessageConverter extends MappingJackson2HttpMessageConverter {
		public DockerRegistryJsonHttpMessageConverter() {
			List<MediaType> mediaTypes = new ArrayList<>();
			mediaTypes.add(MediaType.APPLICATION_JSON);
			mediaTypes.add(new MediaType(APPLICATION_MEDIA_TYPE, APPLICATION_MEDIA_SUBTYPE_DOCKER_MANIFEST));

			setSupportedMediaTypes(mediaTypes);
		}
	}
}
