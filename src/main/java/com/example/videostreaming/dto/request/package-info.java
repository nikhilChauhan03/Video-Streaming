/**
 * <h2>Request DTO Package</h2>
 * <p><strong>Why it exists:</strong> To model user/client inputs for API requests (e.g. creating/updating entities) and holding Bean Validation annotations (like {@code @NotBlank}, {@code @Size}).</p>
 * <p><strong>What belongs here:</strong> Input payloads (e.g. RegisterRequest, VideoUploadRequest), validation metadata, and serialization options.</p>
 * <p><strong>What should never be placed here:</strong> DB annotations (JPA), service logic, database access queries, or output models (which belong in {@code response}).</p>
 */
package com.example.videostreaming.dto.request;
