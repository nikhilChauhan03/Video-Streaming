/**
 * <h2>Entity Package</h2>
 * <p><strong>Why it exists:</strong> To define the persistence model representing database tables mapped via JPA/Hibernate.</p>
 * <p><strong>What belongs here:</strong> Classes annotated with {@code @Entity}, {@code @Table}, and their field mappings (e.g. {@code @Column}, {@code @Id}, {@code @GeneratedValue}, relationships like {@code @OneToMany}).</p>
 * <p><strong>What should never be placed here:</strong> DTO fields, controllers, direct data-access methods (repositories), or client validation configurations (like {@code @Email}).</p>
 */
package com.example.videostreaming.entity;
