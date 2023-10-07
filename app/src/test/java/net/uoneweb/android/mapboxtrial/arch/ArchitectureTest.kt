package net.uoneweb.android.mapboxtrial.arch

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test


class ArchitectureTest {
    @Test
    fun test() {
        val importedClasses = ClassFileImporter().importPackages("net.uoneweb.android.mapboxtrial")
        val rule = noClasses().should().accessClassesThat().resideInAPackage("com.mapbox.maps..")
        rule.check(importedClasses)
    }

    @Test
    fun test2() {
    }
}