package com.retail.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotBlank(message = "Unit is required, e.g. kg, piece, litre")
    private String unit;

    @Positive(message = "Price must be greater than zero")
    @Column(nullable = false)
    private BigDecimal price;

    @PositiveOrZero(message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stockQuantity;

    // Only meaningful for the ALCOHOL category; used to flag age-restricted items.
    @Column(nullable = false)
    private boolean ageRestricted;
}
