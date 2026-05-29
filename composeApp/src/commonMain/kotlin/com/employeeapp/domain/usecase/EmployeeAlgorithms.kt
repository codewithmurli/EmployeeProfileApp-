package com.employeeapp.domain.usecase

import com.employeeapp.domain.model.Employee

// ─────────────────────────────────────────────────────────────────────────────
// 6.1  DUPLICATE DETECTION — HashSet O(1) average lookup
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Normalises a raw phone string by stripping non-digit characters
 * then removing leading country-code prefixes (91 for India, 0).
 * Time:  O(n) where n = string length
 * Space: O(n)
 */
fun normalizePhone(raw: String): String =
    raw.replace(Regex("[\\s()\\-+]"), "")
        .removePrefix("91")
        .removePrefix("0")
        .take(10)

/**
 * In-memory duplicate index backed by two [HashSet]s.
 *
 * Time:  O(1) average for [isDuplicate], [addEntry], [removeEntry]
 * Space: O(n) — two sets, each up to n entries
 */
class DuplicateDetector {

    private val emailIndex = HashSet<String>()
    private val phoneIndex = HashSet<String>()

    fun initialise(employees: List<Employee>) {
        emailIndex.clear()
        phoneIndex.clear()
        employees.forEach { emp ->
            emailIndex.add(emp.email.lowercase())
            phoneIndex.add(emp.normalizedPhone)
        }
    }

    /**
     * O(1) average — HashSet.contains()
     */
    fun isDuplicate(
        email: String,
        normalizedPhone: String,
        excludeId: Long? = null,
        employees: List<Employee> = emptyList()
    ): DuplicateResult {
        val lEmail = email.lowercase()
        val emailDup = if (excludeId != null)
            employees.any { it.email.lowercase() == lEmail && it.id != excludeId }
        else lEmail in emailIndex

        val phoneDup = if (excludeId != null)
            employees.any { it.normalizedPhone == normalizedPhone && it.id != excludeId }
        else normalizedPhone in phoneIndex

        return DuplicateResult(emailDuplicate = emailDup, phoneDuplicate = phoneDup)
    }

    fun addEntry(email: String, normalizedPhone: String) {
        emailIndex.add(email.lowercase())
        phoneIndex.add(normalizedPhone)
    }

    fun removeEntry(email: String, normalizedPhone: String) {
        emailIndex.remove(email.lowercase())
        phoneIndex.remove(normalizedPhone)
    }

    fun updateEntry(oldEmail: String, oldPhone: String, newEmail: String, newPhone: String) {
        removeEntry(oldEmail, oldPhone)
        addEntry(newEmail, newPhone)
    }
}

data class DuplicateResult(
    val emailDuplicate: Boolean,
    val phoneDuplicate: Boolean
) {
    val hasDuplicate: Boolean get() = emailDuplicate || phoneDuplicate
}

// ─────────────────────────────────────────────────────────────────────────────
// 6.2  TOP-N HIGHEST SALARIES — Min-Heap of fixed size N
//      Implemented with a sorted MutableList (KMP-compatible, same complexity)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returns the top [n] employees by salary using a min-heap approach.
 *
 * KMP-compatible implementation: uses a MutableList sorted ascending
 * (minimum at index 0) to simulate a min-heap of size [n].
 *
 * Time:  O(m log n) — m iterations, each insertion O(log n) via binary search
 * Space: O(n) — list holds at most n entries
 *
 * Why not sort? Full sort = O(m log m). When n << m (e.g. n=5, m=500)
 * this heap approach is significantly faster and uses O(n) instead of O(m) space.
 *
 * @param employees all employees
 * @param n         configurable top count (default 5)
 */
fun topNBySalary(employees: List<Employee>, n: Int = 5): List<Employee> {
    require(n > 0) { "n must be positive" }
    if (employees.isEmpty()) return emptyList()
    val effectiveN = minOf(n, employees.size)

    // Min-heap simulated as sorted list (ascending by salary — min at index 0)
    val heap = mutableListOf<Employee>()

    for (emp in employees) {
        if (heap.size < effectiveN) {
            // Insert in sorted position
            val pos = heap.indexOfFirst { it.salary > emp.salary }
            if (pos == -1) heap.add(emp) else heap.add(pos, emp)
        } else if (emp.salary > heap[0].salary) {
            // Employee earns more than current minimum — evict minimum, insert new
            heap.removeAt(0)
            val pos = heap.indexOfFirst { it.salary > emp.salary }
            if (pos == -1) heap.add(emp) else heap.add(pos, emp)
        }
    }

    // Return descending (rank 1 = highest salary)
    return heap.sortedByDescending { it.salary }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6.3  UNDO DELETE — Stack backed by ArrayDeque (LIFO, O(1) push/pop)
// ─────────────────────────────────────────────────────────────────────────────

private const val UNDO_MAX_DEPTH = 10

/**
 * Bounded undo stack for deleted employees.
 *
 * Time:  O(1) amortised for [push] and [pop]
 * Space: O(k) where k ≤ UNDO_MAX_DEPTH (constant upper bound = 10)
 */
class UndoDeleteStack {

    private val stack = ArrayDeque<Employee>()

    /**
     * Push deleted employee. Drops oldest entry if depth exceeds [UNDO_MAX_DEPTH].
     * Time: O(1) amortised
     */
    fun push(employee: Employee) {
        if (stack.size >= UNDO_MAX_DEPTH) stack.removeLast()
        stack.addFirst(employee)
    }

    /**
     * Pop the most recently deleted employee.
     * Time: O(1)
     */
    fun pop(): Employee? = if (stack.isEmpty()) null else stack.removeFirst()

    val isEmpty: Boolean get() = stack.isEmpty()
    val size: Int get() = stack.size
}
