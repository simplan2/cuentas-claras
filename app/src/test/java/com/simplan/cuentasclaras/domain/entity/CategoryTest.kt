package com.simplan.cuentasclaras.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryTest {

    @Test
    fun ids_are_unique() {
        val ids = Category.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun none_has_zero_id() {
        assertEquals(0L, Category.NONE.id)
    }

    @Test
    fun fromId_returns_matching_category() {
        assertEquals(Category.FOOD, Category.fromId(1L))
        assertEquals(Category.OTHER, Category.fromId(10L))
    }

    @Test
    fun fromId_unknown_returns_null() {
        assertNull(Category.fromId(999L))
    }

    @Test
    fun labelOf_returns_label_or_null() {
        assertEquals("Alimentación", Category.labelOf(1L))
        assertNull(Category.labelOf(999L))
    }

    @Test
    fun selectable_excludes_none() {
        assertFalse(Category.selectable.contains(Category.NONE))
        assertEquals(Category.entries.size - 1, Category.selectable.size)
    }
}
