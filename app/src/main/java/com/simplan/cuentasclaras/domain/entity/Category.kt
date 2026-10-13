package com.simplan.cuentasclaras.domain.entity

enum class Category(val id: Long, val label: String) {
    NONE(0L, "Sin categoría"),
    FOOD(1L, "Alimentación"),
    TRANSPORT(2L, "Transporte"),
    HOME(3L, "Hogar"),
    SHOPPING(4L, "Compras"),
    HEALTH(5L, "Salud"),
    EDUCATION(6L, "Educación"),
    LEISURE(7L, "Ocio"),
    SERVICES(8L, "Servicios"),
    SALARY(9L, "Salario"),
    OTHER(10L, "Otros");

    companion object {
        val selectable: List<Category> = entries.filter { it != NONE }

        fun fromId(id: Long): Category? = entries.firstOrNull { it.id == id }

        fun labelOf(id: Long): String? = fromId(id)?.label
    }
}
