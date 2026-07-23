/**
 * <h2>Repository Package</h2>
 * <p><strong>Why it exists:</strong> To abstract the database access layer (persistence operations) using Spring Data JPA.</p>
 * <p><strong>What belongs here:</strong> Interfaces extending {@code JpaRepository} or custom repository classes, annotations like {@code @Repository}, and custom query methods using JPQL/SQL.</p>
 * <p><strong>What should never be placed here:</strong> Service or business calculation logic, controllers, DTO mapping logic, or framework config definitions.</p>
 */
package com.example.videostreaming.repository;
