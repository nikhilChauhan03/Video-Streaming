/**
 * <h2>Service Implementation Package</h2>
 * <p><strong>Why it exists:</strong> To implement the business logic contracts defined in the parent {@code service} package, managing transactional boundaries.</p>
 * <p><strong>What belongs here:</strong> Concrete classes annotated with {@code @Service} implementing service contracts, managing transactions via {@code @Transactional}, and calling repositories/DTO mappers.</p>
 * <p><strong>What should never be placed here:</strong> HTTP request-routing classes (controllers), config settings, or raw database connection classes.</p>
 */
package com.example.videostreaming.service.impl;
