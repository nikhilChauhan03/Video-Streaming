/**
 * <h2>Controller Package</h2>
 * <p><strong>Why it exists:</strong> The presentation layer. Exposes REST API endpoints and maps incoming HTTP requests to corresponding backend handlers.</p>
 * <p><strong>What belongs here:</strong> Classes annotated with {@code @RestController}, request mappings ({@code @GetMapping}, {@code @PostMapping}, etc.), path variable/request param extracts, request validation triggers ({@code @Valid}), and handling HTTP response statuses.</p>
 * <p><strong>What should never be placed here:</strong> Business logic, raw database access, manual transactional scopes, or JPA entities (map requests/responses directly to DTOs first).</p>
 */
package com.example.videostreaming.controller;
