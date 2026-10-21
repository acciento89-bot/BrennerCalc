package de.kamilunavo.brennercalc

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.io.File

class ServiceVisitsTest {
    private val at = 1_800_000_000_000L
    private fun oil(nozzle: Double = 0.5) = VisitSnapshot.capture(VisitKind.OIL, mapOf("nozzle" to nozzle, "pressure" to 7.0, "calorific" to 10.0, "efficiency" to 90.0), "Before", "Nameplate", at)

    @Test fun allFiveModesKeepExistingFormulas() {
        val before = oil()
        assertEquals(17.034353028, before.results.getValue("output"), 0.000001)
        assertEquals(3.4068706056, oil(0.6).difference(before)!!.getValue("output"), 0.000001)
        val reverse = VisitSnapshot.capture(VisitKind.OIL_REVERSE, mapOf("power" to 17.034353028, "pressure" to 7.0, "calorific" to 10.0, "efficiency" to 90.0), "", "", at)
        assertEquals(0.5, reverse.results.getValue("nozzle"), 0.000001)
        val gas = VisitSnapshot.capture(VisitKind.GAS, mapOf("flow" to 2.0, "calorific" to 10.0, "efficiency" to 90.0), "", "", at)
        assertEquals(18.0, gas.results.getValue("output"), 0.000001)
        val gasReverse = VisitSnapshot.capture(VisitKind.GAS_REVERSE, mapOf("power" to 18.0, "calorific" to 10.0, "efficiency" to 90.0), "", "", at)
        assertEquals(2.0, gasReverse.results.getValue("flow"), 0.000001)
        val water = VisitSnapshot.capture(VisitKind.WATER, mapOf("power" to 20.0, "deltaT" to 20.0), "", "", at)
        assertEquals(859.8452278589854, water.results.getValue("flow"), 0.000001)
        assertNull(water.difference(before))
        assertNull(reverse.difference(before))
    }
    @Test fun invalidValuesNeverBecomeSavedVisits() {
        for (invalid in listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0)) {
            assertThrows(IllegalArgumentException::class.java) { oil(invalid) }
        }
        assertThrows(IllegalArgumentException::class.java) { VisitSnapshot.capture(VisitKind.WATER, mapOf("power" to 20.0, "deltaT" to 0.0), "", "", at) }
        assertThrows(IllegalArgumentException::class.java) { VisitSnapshot.capture(VisitKind.GAS_REVERSE, mapOf("power" to 20.0, "calorific" to 0.0, "efficiency" to 90.0), "", "", at) }
        assertThrows(IllegalArgumentException::class.java) { VisitSnapshot.capture(VisitKind.GAS, emptyMap(), "", "", at) }
    }
    @Test fun atomicArchiveRoundtripMigrationAndCorruptionProtection() {
        val folder = Files.createTempDirectory("brenner-visits").toFile()
        try {
            val file = File(folder, "nested/visits.json")
            val archive = VisitArchive(file)
            assertTrue(archive.load().isEmpty())
            val projects = listOf(ServiceProject(name = "Boiler A", notes = "Basement", visits = listOf(oil())))
            archive.save(projects)
            assertEquals(projects, VisitArchive(file).load())
            val report = projects[0].report("en")
            assertTrue(report.contains("Nameplate") && report.contains("Before") && report.contains("kW") && report.contains("Not a certification"))
            assertTrue(projects[0].report("de").contains("Eingegebene Werte"))
            val valid = file.readText()
            file.writeText("{broken")
            assertThrows(Exception::class.java) { archive.load() }
            assertThrows(Exception::class.java) { archive.save(emptyList()) }
            assertEquals("{broken", file.readText())
            file.writeText("{\"schemaVersion\":999,\"projects\":[]}")
            assertThrows(Exception::class.java) { archive.load() }
            assertThrows(Exception::class.java) { archive.save(emptyList()) }
            file.writeText(valid.substringAfter("\"projects\":").dropLast(1))
            assertEquals(projects, archive.load())
            archive.save(projects)
            assertTrue(file.readText().contains("schemaVersion"))
        } finally { folder.deleteRecursively() }
    }
    @Test fun failedStoreMutationPreservesVisibleStateAndCorruptBytes() {
        val folder = Files.createTempDirectory("brenner-store").toFile()
        try {
            val file = File(folder, "visits.json")
            val store = ServiceVisitStore(file)
            val projects = listOf(ServiceProject(name = "Boiler", notes = "", visits = listOf(oil())))
            assertTrue(store.commit(projects))
            file.writeText("{broken")
            assertFalse(store.commit(emptyList()))
            assertEquals(projects, store.projects)
            assertEquals("{broken", file.readText())
            val reopened = ServiceVisitStore(file)
            assertTrue(reopened.loadFailed)
            assertFalse(reopened.commit(emptyList()))
            assertEquals("{broken", file.readText())
        } finally { folder.deleteRecursively() }
    }
    @Test fun failedWriteDoesNotCreateAReplacementArchive() {
        val folder = Files.createTempDirectory("brenner-visits-failure").toFile()
        try {
            val parent = File(folder, "blocked").apply { writeText("preserve") }
            assertThrows(Exception::class.java) { VisitArchive(File(parent, "visits.json")).save(emptyList()) }
            assertEquals("preserve", parent.readText())
        } finally { folder.deleteRecursively() }
    }
}
