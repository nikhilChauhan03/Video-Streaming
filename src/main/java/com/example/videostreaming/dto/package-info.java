/**
 * <h2>Data Transfer Objects (DTO) Package</h2>
 * <p><strong>Why it exists:</strong> To define container objects used to transfer data between the presentation layer (Controller) and the service/persistence layers, encapsulating validation rules and decoupling external representation from the database structure.</p>
 * <p><strong>What belongs here:</strong> Base DTO properties or shared model payloads.</p>
 * <p><strong>What should never be placed here:</strong> JPA database entities, repository access, REST endpoints, or business logic.</p>
 */
package com.example.videostreaming.dto;
