import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.6.1"
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.grammarkit") version "2019.1"
}

group = "cappuccino.ide.intellij.plugin"
version = "0.4.5"

repositories {
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

}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2019.1"
    updateSinceUntilBuild = false
    sameSinceUntilBuild = true
    sandboxDirectory = "/Users/daniel/Projects/Intellij Sandbox"
    setPlugins("PsiViewer:191.4212")
}

tasks.withType<org.jetbrains.intellij.tasks.RunIdeTask>().all {
    maxHeapSize = "1g"
    autoReloadPlugins = true
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