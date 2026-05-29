package com.employeeapp

import com.employeeapp.domain.model.Department
import com.employeeapp.domain.model.Employee
import com.employeeapp.domain.model.EmploymentType
import com.employeeapp.domain.model.Gender
import com.employeeapp.domain.model.Skill
import com.employeeapp.domain.usecase.DuplicateDetector
import com.employeeapp.domain.usecase.FormValidation
import com.employeeapp.domain.usecase.UndoDeleteStack
import com.employeeapp.domain.usecase.normalizePhone
import com.employeeapp.domain.usecase.topNBySalary
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Test helpers ──────────────────────────────────────────────────────────────

private fun makeEmployee(
    id: Long = 1,
    name: String = "Test User",
    email: String = "test@example.com",
    salary: Double = 50_000.0,
    normalizedPhone: String = "9876543210"
) = Employee(
    id = id,
    fullName = name,
    email = email,
    phone = normalizedPhone,
    normalizedPhone = normalizedPhone,
    address = "123 Test Street",
    gender = Gender.MALE,
    department = Department.ENGINEERING,
    skills = listOf(Skill.KOTLIN),
    employmentType = EmploymentType.FULL_TIME,
    isActive = true,
    joiningDate = LocalDate(2023, 1, 15),
    salary = salary
)

// ─────────────────────────────────────────────────────────────────────────────
// Phone normalisation tests
// ─────────────────────────────────────────────────────────────────────────────

class PhoneNormalisationTest {

    @Test
    fun `normalizePhone strips spaces and dashes`() {
        assertEquals("9876543210", normalizePhone("98765 43210"))
        assertEquals("9876543210", normalizePhone("9876-543210"))
    }

    @Test
    fun `normalizePhone strips country code 91`() {
        assertEquals("9876543210", normalizePhone("+919876543210"))
        assertEquals("9876543210", normalizePhone("919876543210"))
    }

    @Test
    fun `normalizePhone strips leading zero`() {
        assertEquals("9876543210", normalizePhone("09876543210"))
    }

    @Test
    fun `normalizePhone strips parentheses`() {
        assertEquals("9876543210", normalizePhone("(98765)43210"))
    }

    @Test
    fun `normalizePhone handles plain 10-digit number`() {
        assertEquals("9876543210", normalizePhone("9876543210"))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DuplicateDetector (HashSet) tests
// ─────────────────────────────────────────────────────────────────────────────

class DuplicateDetectorTest {

    private fun buildDetector(employees: List<Employee>): DuplicateDetector {
        val d = DuplicateDetector()
        d.initialise(employees)
        return d
    }

    @Test
    fun `isDuplicate returns email duplicate when email exists`() {
        val emp = makeEmployee(email = "alice@corp.com")
        val detector = buildDetector(listOf(emp))
        val result = detector.isDuplicate("alice@corp.com", "0000000000")
        assertTrue(result.emailDuplicate)
        assertFalse(result.phoneDuplicate)
    }

    @Test
    fun `isDuplicate returns phone duplicate when phone exists`() {
        val emp = makeEmployee(normalizedPhone = "9876543210")
        val detector = buildDetector(listOf(emp))
        val result = detector.isDuplicate("new@corp.com", "9876543210")
        assertFalse(result.emailDuplicate)
        assertTrue(result.phoneDuplicate)
    }

    @Test
    fun `isDuplicate is case-insensitive for email`() {
        val emp = makeEmployee(email = "ALICE@corp.com")
        val detector = buildDetector(listOf(emp))
        val result = detector.isDuplicate("alice@corp.com", "0000000000")
        assertTrue(result.emailDuplicate)
    }

    @Test
    fun `isDuplicate returns false when no duplicates`() {
        val emp = makeEmployee(email = "alice@corp.com", normalizedPhone = "1111111111")
        val detector = buildDetector(listOf(emp))
        val result = detector.isDuplicate("bob@corp.com", "2222222222")
        assertFalse(result.hasDuplicate)
    }

    @Test
    fun `addEntry then isDuplicate detects new entry`() {
        val detector = buildDetector(emptyList())
        detector.addEntry("charlie@corp.com", "9999999999")
        val result = detector.isDuplicate("charlie@corp.com", "0000000000")
        assertTrue(result.emailDuplicate)
    }

    @Test
    fun `removeEntry then isDuplicate clears entry`() {
        val emp = makeEmployee(email = "dave@corp.com", normalizedPhone = "8888888888")
        val detector = buildDetector(listOf(emp))
        detector.removeEntry("dave@corp.com", "8888888888")
        val result = detector.isDuplicate("dave@corp.com", "8888888888")
        assertFalse(result.hasDuplicate)
    }

    @Test
    fun `updateEntry replaces old entry with new`() {
        val emp = makeEmployee(email = "eve@corp.com", normalizedPhone = "7777777777")
        val detector = buildDetector(listOf(emp))
        detector.updateEntry("eve@corp.com", "7777777777", "eve2@corp.com", "6666666666")
        assertFalse(detector.isDuplicate("eve@corp.com", "7777777777").hasDuplicate)
        assertTrue(detector.isDuplicate("eve2@corp.com", "0000000000").emailDuplicate)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top-N Salary (Min-Heap) tests
// ─────────────────────────────────────────────────────────────────────────────

class TopNSalaryTest {

    private fun salaryList(vararg salaries: Double): List<Employee> =
        salaries.mapIndexed { i, s ->
            makeEmployee(id = i.toLong(), salary = s, email = "emp$i@corp.com", normalizedPhone = "000000000$i")
        }

    @Test
    fun `topNBySalary returns correct top-5`() {
        val employees = salaryList(10000.0, 90000.0, 50000.0, 80000.0, 30000.0, 70000.0, 60000.0)
        val top5 = topNBySalary(employees, 5)
        assertEquals(5, top5.size)
        assertEquals(90000.0, top5[0].salary)
        assertEquals(80000.0, top5[1].salary)
        assertEquals(70000.0, top5[2].salary)
    }

    @Test
    fun `topNBySalary returns descending order`() {
        val employees = salaryList(100.0, 500.0, 200.0, 400.0, 300.0)
        val top3 = topNBySalary(employees, 3)
        assertEquals(3, top3.size)
        assertTrue(top3[0].salary >= top3[1].salary)
        assertTrue(top3[1].salary >= top3[2].salary)
    }

    @Test
    fun `topNBySalary with n larger than list returns all`() {
        val employees = salaryList(100.0, 200.0, 300.0)
        val top10 = topNBySalary(employees, 10)
        assertEquals(3, top10.size)
    }

    @Test
    fun `topNBySalary with empty list returns empty`() {
        val top5 = topNBySalary(emptyList(), 5)
        assertTrue(top5.isEmpty())
    }

    @Test
    fun `topNBySalary n=1 returns single highest`() {
        val employees = salaryList(1000.0, 5000.0, 3000.0)
        val top1 = topNBySalary(employees, 1)
        assertEquals(1, top1.size)
        assertEquals(5000.0, top1[0].salary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UndoDeleteStack tests
// ─────────────────────────────────────────────────────────────────────────────

class UndoDeleteStackTest {

    @Test
    fun `push and pop returns same employee`() {
        val stack = UndoDeleteStack()
        val emp = makeEmployee()
        stack.push(emp)
        val popped = stack.pop()
        assertEquals(emp.id, popped?.id)
    }

    @Test
    fun `pop from empty stack returns null`() {
        val stack = UndoDeleteStack()
        assertNull(stack.pop())
    }

    @Test
    fun `LIFO order maintained`() {
        val stack = UndoDeleteStack()
        val emp1 = makeEmployee(id = 1)
        val emp2 = makeEmployee(id = 2, email = "b@b.com", normalizedPhone = "1111111111")
        stack.push(emp1)
        stack.push(emp2)
        assertEquals(2L, stack.pop()?.id)
        assertEquals(1L, stack.pop()?.id)
    }

    @Test
    fun `stack respects max depth of 10`() {
        val stack = UndoDeleteStack()
        repeat(12) { i ->
            stack.push(makeEmployee(id = i.toLong(), email = "emp$i@corp.com", normalizedPhone = "000000000$i"))
        }
        assertEquals(10, stack.size)
    }

    @Test
    fun `isEmpty returns true when stack is empty`() {
        val stack = UndoDeleteStack()
        assertTrue(stack.isEmpty)
        stack.push(makeEmployee())
        assertFalse(stack.isEmpty)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form validation tests
// ─────────────────────────────────────────────────────────────────────────────

class FormValidationTest {

    @Test
    fun `validateFullName accepts valid name`() {
        assertNull(FormValidation.validateFullName("John Doe", emptyList()))
    }

    @Test
    fun `validateFullName rejects blank`() {
        assertNotNull(FormValidation.validateFullName("", emptyList()))
    }

    @Test
    fun `validateFullName rejects less than 3 chars`() {
        assertNotNull(FormValidation.validateFullName("Jo", emptyList()))
    }

    @Test
    fun `validateEmail accepts valid email`() {
        assertNull(FormValidation.validateEmail("user@example.com"))
    }

    @Test
    fun `validateEmail rejects invalid format`() {
        assertNotNull(FormValidation.validateEmail("notanemail"))
        assertNotNull(FormValidation.validateEmail("missing@domain"))
        assertNotNull(FormValidation.validateEmail("@nodomain.com"))
    }

    @Test
    fun `validatePhone accepts 10-digit number`() {
        assertNull(FormValidation.validatePhone("9876543210"))
    }

    @Test
    fun `validatePhone rejects non-10-digit`() {
        assertNotNull(FormValidation.validatePhone("12345"))
        assertNotNull(FormValidation.validatePhone("12345678901"))
    }

    @Test
    fun `validateSalary accepts positive number`() {
        assertNull(FormValidation.validateSalary("50000"))
        assertNull(FormValidation.validateSalary("75000.50"))
    }

    @Test
    fun `validateSalary rejects zero or negative`() {
        assertNotNull(FormValidation.validateSalary("0"))
        assertNotNull(FormValidation.validateSalary("-1000"))
    }

    @Test
    fun `validateAddress rejects short address`() {
        assertNotNull(FormValidation.validateAddress("A"))
        assertNull(FormValidation.validateAddress("123 Main Street"))
    }
}
