package com.felixhoner.schichtplaner.api.persistence.entity

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "production")
data class ProductionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotBlank
    val name: String,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    val plan: PlanEntity,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @OneToMany(mappedBy = "production", fetch = FetchType.LAZY)
    val shifts: MutableList<ShiftEntity> = mutableListOf()
) {
}
