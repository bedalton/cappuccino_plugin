import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "1.1.4"
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.grammarkit") version "2021.1.3"
}

group = "cappuccino.ide.intellij.plugin"
version = "2022.3.0"
val verifyPluginIDEDownloadDir: String? by project

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    gradlePluginPortal()
    mavenCentral()
}
sourceSets.main {
    java.srcDirs("src/main/java", "gen")
    kotlin.sourceSets {
        this.register("gen")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {

    tasks.withType<KotlinCompile>().all {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xjvm-default=enable",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.OptIn",
            "-Xopt-in=kotlin.ExperimentalMultiplatform",
            "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xopt-in=kotlin.contracts.ExperimentalContracts",
            "-Xopt-in=ExperimentalJsExport"
        )
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalJsExport")
            languageSettings.useExperimentalAnnotation("kotlin.js.ExperimentalJsExport")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalMultiplatform")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }

}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2020.1")
    updateSinceUntilBuild.set(false)
    sameSinceUntilBuild.set(true)
    sandboxDir.set("/Users/daniel/Projects/Intellij Sandbox")
    plugins.set(listOf("PsiViewer:201-SNAPSHOT"))//, "com.mallowigi.idea:10.0"))

}


tasks.withType<org.jetbrains.intellij.tasks.RunPluginVerifierTask>().all {
    this.ideVersions.set(listOf(
        "IU-191.8026.42",
        "IC-191.8026.42",
        "IU-212.4037.9",
        "IC-212.4037.9",
        "IU-211.7142.13",
        "IC-211.7142.13",
        "IU-202.6948.69",
        "IC-202.6948.69",
        "PS-211.7628.25",
        "PS-191.8026.56"
    ))
    if (verifyPluginIDEDownloadDir != null) {
        this.downloadDir.set(verifyPluginIDEDownloadDir)
    }

}

tasks.withType<org.jetbrains.intellij.tasks.RunIdeTask>().all {
    maxHeapSize = "1g"
    autoReloadPlugins.set(true)
}

tasks.register<GenerateLexer>("generateObjJLexer") {
    source = "src/main/resources/grammar/_ObjJLexer.flex"
    targetDir = "gen/cappuccino/ide/intellij/plugin/lexer"
    targetClass = "_ObjJLexer"
    purgeOldFiles = true
}

tasks.register<GenerateLexer>("generateJsTypDefLexer") {
    source = "src/main/resources/grammar/_JsTypeDefLexer.flex"
    targetDir = "gen/cappuccino/ide/intellij/plugin/jstypedef/lexer"
    targetClass = "_JsTypeDefLexer"
    purgeOldFiles = true
}

tasks.register<GenerateLexer>("generateDocCommentLexer") {
    source = "src/main/resources/grammar/_ObjJDocCommentLexer.flex"
    targetDir = "gen/cappuccino/ide/intellij/plugin/comments/lexer"
    targetClass = "_ObjJDocCommentLexer"
    purgeOldFiles = true
}

val generateLexers = tasks.register<Task>("generateLexers") {
    dependsOn("generateObjJLexer")
    dependsOn("generateJsTypDefLexer")
    dependsOn("generateDocCommentLexer")
}

//
//fun generateObjJParserTask(suffix: String = "", config: GenerateParser.() -> Unit = {}) =
//    task<GenerateParser>("generateObjJParser${suffix}") {
//        source = "src/main/resources/grammar/ObjJ.bnf"
//        targetRoot = "gen"
//        pathToParser = "cappuccino/ide/intellij/plugin/parser/ObjectiveJParser"
//        pathToPsiRoot = "cappuccino/ide/intellij/plugin/psi"
//        purgeOldFiles = true
//        config()
//    }
//
//fun generateJsTypeDefParserTask(suffix: String = "", config: GenerateParser.() -> Unit = {}) =
//    task<GenerateParser>("JsTypeDefParser${suffix}") {
//        source = "src/main/resources/grammar/SimpleJsTypeDef.bnf"
//        targetRoot = "gen"
//        pathToParser = "cappuccino/ide/intellij/plugin/jstypedef/parser/JsTypeDefParser"
//        pathToPsiRoot = "cappuccino/ide/intellij/plugin/jstypedef/psi"
//        purgeOldFiles = true
//        config()
//    }
//
//fun generateDocCommentParserTask(suffix: String = "", config: GenerateParser.() -> Unit = {}) =
//    task<GenerateParser>("generateDocCommentParser${suffix}") {
//        source = "src/main/resources/grammar/ObjJDocComment.bnf"
//        targetRoot = "gen"
//        pathToParser = "cappuccino/ide/intellij/plugin/comments/parser/ObjJDocCommentParser"
//        pathToPsiRoot = "cappuccino/ide/intellij/plugin/comments/psi"
//        purgeOldFiles = true
//        config()
//    }
//
//fun generateParsersTask(suffix: String = "", config: GenerateParser.() -> Unit = {}) =
//    task<Task>("generateParsers${suffix}") {
//        val jsTypeDef = generateJsTypeDefParserTask(suffix, config)
//        val objJ = generateObjJParserTask(suffix, config).apply {
//            mustRunAfter(jsTypeDef)
//        }
//        val docComment = generateDocCommentParserTask(suffix, config).apply {
//            mustRunAfter(objJ)
//        }
//        dependsOn(objJ, jsTypeDef, docComment)
//    }
//
//val generateParsersInitial = generateParsersTask("Initial")
//
//val compileKotlin = tasks.named("compileKotlin") {
//    dependsOn(generateLexers, generateParsersInitial)
//}
//
//val generateParser = generateParsersTask {
//    //dependsOn(compileKotlin)
//    classpath(compileKotlin.get().outputs)
//}
//
//tasks.named("compileJava") {
//    dependsOn(generateParser)
//}