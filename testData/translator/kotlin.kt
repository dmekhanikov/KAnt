import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.types.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.mappers.cutdirsmapper
import jetbrains.kant.dsl.types.resources.file

val manifestImplVendor by StringProperty("manifest.impl.vendor")
val buildNumber by StringProperty("build.number") { "snapshot" }
val javaHome by StringProperty("java.home")
val externalAnnotationsPath by StringProperty("external.annotations.path") { "$basedir/annotations" }
val generateJavadoc by BooleanProperty("generate.javadoc") { true }
val basedir by StringProperty()
val outputName by StringProperty("output.name") { "kotlin-compiler-$buildNumber" }
val manifestImplTitleKotlinCompiler by StringProperty("manifest.impl.title.kotlin.compiler")
val generateAssertions by BooleanProperty("generate.assertions") { true }
val kotlinHome by StringProperty("kotlin-home") { "$output/kotlinc" }
val dependenciesDir by StringProperty("dependencies.dir") { "$basedir/dependencies" }
val manifestImplTitleKotlinCompilerJavadoc by StringProperty("manifest.impl.title.kotlin.compiler.javadoc")
val jssejar by StringProperty()
val rtjar by StringProperty()
val bootstrapCompilerHome by StringProperty("bootstrap.compiler.home") { "$bootstrapHome/Kotlin/kotlinc" }
val javaTarget by DoubleProperty("java.target") { 1.6 }
val manifestImplTitleKotlinCompilerSources by StringProperty("manifest.impl.title.kotlin.compiler.sources")
val bootstrapHome by StringProperty("bootstrap.home") { "dependencies/bootstrap-compiler" }
val ideaSdk by StringProperty("idea.sdk") { "$basedir/ideaSDK" }
var ideaOut by StringProperty("idea.out")
val manifestImplTitleKotlinJavascriptStdlib by StringProperty("manifest.impl.title.kotlin.javascript.stdlib")
val outputRelative by StringProperty("output.relative") { "dist" }
val bootstrapRuntime by StringProperty("bootstrap.runtime") { "$bootstrapCompilerHome/lib/kotlin-runtime.jar" }
val manifestImplTitleKotlinCompilerAntTask by StringProperty("manifest.impl.title.kotlin.compiler.ant.task")
val manifestImplTitleKotlinPreloader by StringProperty("manifest.impl.title.kotlin.preloader")
val manifestImplTitleKotlinJvmRuntimeSources by StringProperty("manifest.impl.title.kotlin.jvm.runtime.sources")
val bootstrapBuildNoTests by BooleanProperty("bootstrap.build.no.tests") { false }
val shrink by BooleanProperty() { true }
val output by StringProperty() { "$basedir/$outputRelative" }
val manifestImplTitleKotlinJvmRuntime by StringProperty("manifest.impl.title.kotlin.jvm.runtime")
var compilerManifestClassPath by StringProperty("compiler.manifest.class.path")

fun DSLTaskContainer.cleandir(dir: String) {
    echo(message = "Cleaning $dir")
    delete(dir = dir, failOnError = false)
    mkdir(dir = dir)
}

fun DSLTaskContainer.packCompiler(jarfile: String, compress: Boolean = true) {
    if (bootstrapBuildNoTests) {
        compilerManifestClassPath = "."
    } else {
        compilerManifestClassPath = "kotlin-runtime.jar"
    }
    jar(jarfile = jarfile, compress = compress, duplicate = "preserve") {
        zipfileset(src = bootstrapRuntime) {
            include(name = "**/*.class", `if` = bootstrapBuildNoTests.toString())
        }
        fileset(dir = "$output/classes/compiler")
        fileset(dir = "$output/builtins") {
            include(name = "kotlin/**")
            exclude(name = "kotlin/internal/**")
        }
        fileset(dir = "$basedir/compiler/frontend.java/src", includes = "META-INF/services/**")
        fileset(dir = "$basedir/compiler/backend/src", includes = "META-INF/services/**")
        zipGroupFileset(dir = "$basedir/lib", includes = "*.jar")
        zipGroupFileset(dir = "$basedir/ideaSDK/core", includes = "*.jar", excludes = "util.jar")
        zipGroupFileset(dir = "$basedir/ideaSDK/lib", includes = "jna-utils.jar")
        zipGroupFileset(dir = "$basedir/ideaSDK/lib", includes = "oromatcher.jar")
        zipGroupFileset(dir = "$basedir/ideaSDK/lib", includes = "protobuf-2.5.0.jar")
        zipGroupFileset(dir = "$basedir/ideaSDK/jps", includes = "jps-model.jar")
        zipGroupFileset(dir = dependenciesDir, includes = "jline.jar")
        zipGroupFileset(dir = dependenciesDir, includes = "cli-parser-1.1.1.jar")
        manifest {
            attribute(name = "Built-By", value = manifestImplVendor)
            attribute(name = "Implementation-Vendor", value = manifestImplVendor)
            attribute(name = "Implementation-Title", value = manifestImplTitleKotlinCompiler)
            attribute(name = "Implementation-Version", value = buildNumber)
            attribute(name = "Class-Path", value = compilerManifestClassPath)
            attribute(name = "Main-Class", value = "org.jetbrains.jet.cli.jvm.K2JVMCompiler")
        }
    }
}

fun DSLTaskContainer.newKotlinc(src: String, output: String, classpath: String) {
    cleandir(dir = output)
    java(classname = "org.jetbrains.jet.cli.jvm.K2JVMCompiler", failonerror = true, fork = true) {
        classpath {
            path(refid = classpath)
            pathElement(location = "$kotlinHome/lib/kotlin-compiler.jar")
        }
        assertions {
            enable()
        }
        arg(line = src)
        arg(value = "-d")
        arg(value = output)
        arg(value = "-no-stdlib")
        arg(value = "-classpath")
        arg(value = classpath)
    }
}

object project : DSLProject() {
    {
        property(file = "resources/manifest.properties")
    }
    val classpath = path {
        file(file = bootstrapRuntime)
        fileset(dir = ideaSdk, includes = "core/*.jar")
        fileset(dir = ideaSdk, includes = "lib/protobuf-2.5.0.jar")
        fileset(dir = "$basedir/lib", includes = "**/*.jar")
        fileset(dir = dependenciesDir, includes = "jline.jar")
        fileset(dir = dependenciesDir, includes = "jansi.jar")
        fileset(dir = dependenciesDir, includes = "cli-parser-1.1.1.jar")
        fileset(dir = "$basedir/ideaSDK/jps", includes = "jps-model.jar")
    };
    {
        typedef(resource = "org/jetbrains/jet/buildtools/ant/antlib.xml", classpath = "$bootstrapCompilerHome/lib/kotlin-ant.jar")
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties", classpath = "$dependenciesDir/ant-contrib.jar")
        taskdef(name = "javac2", classname = "org.apache.tools.ant.taskdefs.Javac")
        if (generateAssertions) {
            val javac2Classpath = path {
                pathElement(location = "$ideaSdk/lib/javac2.jar")
                pathElement(location = "$ideaSdk/lib/asm-all.jar")
            }
            taskdef(name = "javac2", classname = "com.intellij.ant.Javac2", classpathRef = javac2Classpath)
            echo(message = "Use javac2 from Idea lib")
        } else {
            echo(message = "Use default javac compiler")
        }
    }
    val compilerSourcesDirset = dirset(dir = "$basedir/") {
        include(name = "compiler/frontend/src")
        include(name = "core/descriptors/src")
        include(name = "core/serialization/src")
        include(name = "core/descriptor.loader.java/src")
        include(name = "compiler/frontend.java/src")
        include(name = "core/serialization.java/src")
        include(name = "compiler/backend-common/src")
        include(name = "compiler/backend/src")
        include(name = "compiler/cli/src")
        include(name = "compiler/cli/cli-common/src")
        include(name = "compiler/util/src")
        include(name = "core/util.runtime/src")
        include(name = "compiler/jet.as.java.psi/src")
        include(name = "compiler/builtins-serializer")
        include(name = "js/js.dart-ast/src")
        include(name = "js/js.translator/src")
        include(name = "js/js.frontend/src")
    };
    {
        ideaOut = "$basedir/out/production"
    }
    val compilerClassesFromIDEAFileset = patternset {
        include(name = "frontend/**")
        include(name = "descriptors/**")
        include(name = "serialization/**")
        include(name = "descriptor.loader.java/**")
        include(name = "frontend.java/**")
        include(name = "serialization.java/**")
        include(name = "backend/**")
        include(name = "backend-common/**")
        include(name = "cli/**")
        include(name = "cli-common/**")
        include(name = "util/**")
        include(name = "util.runtime/**")
        include(name = "jet.as.java.psi/**")
        include(name = "builtins-serializer/**")
        include(name = "js.dart-ast/**")
        include(name = "js.translator/**")
        include(name = "js.frontend/**")
    }
    val compilerSourcesPath = path {
        dirset(refid = compilerSourcesDirset)
    }
    val preloaderSourcesPath = path {
        dirset(dir = "compiler/preloader/src")
    }

    val clean = target {
        delete(dir = output)
    }

    val init = target {
        mkdir(dir = kotlinHome)
        mkdir(dir = "$kotlinHome/lib")
    }

    val prepareDist = target {
        copy(todir = "$kotlinHome/bin") {
            fileset(dir = "$basedir/compiler/cli/bin")
        }
        fixcrlf(srcdir = "$kotlinHome/bin", excludes = "**/*.bat", eol = "unix")
        copy(todir = "$kotlinHome/license") {
            fileset(dir = "$basedir/license")
        }
        echo(file = "$kotlinHome/build.txt", message = buildNumber)
        chmod(dir = "$kotlinHome/bin", excludes = "**/*.bat", perm = "755")
    }

    val compilerSources = target {
        jar(jarfile = "$output/kotlin-compiler-sources.jar") {
            fileset(dir = "compiler/frontend/src")
            fileset(dir = "core/descriptors/src")
            fileset(dir = "core/descriptor.loader.java/src")
            fileset(dir = "core/serialization/src")
            fileset(dir = "core/serialization.java/src")
            fileset(dir = "compiler/frontend.java/src")
            fileset(dir = "compiler/backend-common/src")
            fileset(dir = "compiler/backend/src")
            fileset(dir = "compiler/cli/src")
            fileset(dir = "j2k/src")
            fileset(dir = "compiler/util/src")
            fileset(dir = "core/util.runtime/src")
            fileset(dir = "compiler/jet.as.java.psi/src")
            fileset(dir = "compiler/builtins-serializer")
            fileset(dir = "js/js.dart-ast/src")
            fileset(dir = "js/js.translator/src")
            fileset(dir = "js/js.frontend/src")
            zipfileset(file = "$kotlinHome/build.txt", prefix = "META-INF")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinCompilerSources)
                attribute(name = "Implementation-Version", value = buildNumber)
            }
        }
        if (generateJavadoc) {
            delete(dir = "$output/kotlin-compiler-javadoc", failOnError = false)
            javadoc(destdir = "$output/kotlin-compiler-javadoc", sourcepathRef = compilerSourcesPath, classpathRef = classpath, linksource = true, windowtitle = manifestImplTitleKotlinCompiler)
            jar(jarfile = "$output/kotlin-compiler-javadoc.jar") {
                fileset(dir = "$output/kotlin-compiler-javadoc")
                zipfileset(file = "$kotlinHome/build.txt", prefix = "META-INF")
                manifest {
                    attribute(name = "Built-By", value = manifestImplVendor)
                    attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                    attribute(name = "Implementation-Title", value = manifestImplTitleKotlinCompilerJavadoc)
                    attribute(name = "Implementation-Version", value = buildNumber)
                }
            }
        } else {
            jar(jarfile = "$output/kotlin-compiler-javadoc.jar") {
                manifest {
                    attribute(name = "Built-By", value = manifestImplVendor)
                    attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                    attribute(name = "Implementation-Title", value = manifestImplTitleKotlinCompilerJavadoc)
                    attribute(name = "Implementation-Version", value = buildNumber)
                }
            }
        }
    }

    val jslib = target {
        jar(jarfile = "$kotlinHome/lib/kotlin-jslib.jar") {
            fileset(dir = "$basedir/js/js.libraries/src") {
                include(name = "core/**")
                include(name = "jquery/**")
                include(name = "dom/**")
                include(name = "html5/**")
                include(name = "stdlib/TuplesCode.kt")
            }
            fileset(dir = "$basedir/core/reflection/src") {
                include(name = "kotlin/**")
            }
            zipfileset(file = "$kotlinHome/build.txt", prefix = "META-INF")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinJavascriptStdlib)
                attribute(name = "Implementation-Version", value = buildNumber)
            }
        }
        taskdef(name = "closure-compiler", classname = "com.google.javascript.jscomp.ant.CompileTask", classpath = "$dependenciesDir/closure-compiler.jar")
        closureCompiler(compilationLevel = "simple", prettyprint = "true", languagein = "ECMASCRIPT5_STRICT", warning = "verbose", debug = "false", output = "$kotlinHome/lib/kotlin.js") {
            sources(dir = "$basedir/js/js.translator/testData") {
                file(name = "kotlin_lib_ecma5.js")
                file(name = "kotlin_lib.js")
                file(name = "maps.js")
            }
        }
    }

    val preloader = target {
        cleandir(dir = "$output/classes/preloader")
        javac2(destdir = "$output/classes/preloader", debug = "true", debuglevel = "lines,vars,source", includeAntRuntime = "false", source = javaTarget.toString(), target = javaTarget.toString()) {
            src(refid = "preloaderSources.path")
        }
        jar(jarfile = "$kotlinHome/lib/kotlin-preloader.jar") {
            fileset(dir = "$output/classes/preloader")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinPreloader)
                attribute(name = "Implementation-Version", value = buildNumber)
                attribute(name = "Main-Class", value = "org.jetbrains.jet.preloading.Preloader")
            }
        }
    }

    val builtins = target {
        cleandir(dir = "$output/builtins")
        java(classname = "org.jetbrains.jet.utils.builtinsSerializer.BuiltinsSerializerPackage", classpath = "$bootstrapCompilerHome/lib/kotlin-compiler.jar", failonerror = true, fork = true) {
            assertions {
                enable()
            }
            arg(value = "$output/builtins")
            arg(value = "core/builtins/native")
            arg(value = "core/builtins/src")
        }
    }

    val compilerQuick = target("compiler_quick") {
        delete(dir = "$output/classes/compiler")
        copy(todir = "$output/classes/compiler") {
            fileset(dir = "$ideaOut/") {
                patternSet(refid = compilerClassesFromIDEAFileset)
            }
            cutdirsmapper(dirs = 1)
        }
        delete(file = "$kotlinHome/lib/kotlin-compiler.jar")
        packCompiler(jarfile = "$kotlinHome/lib/kotlin-compiler.jar", compress = false)
    }

    val compiler = target {
        taskdef(resource = "proguard/ant/task.properties", classpath = "$dependenciesDir/proguard.jar")
        cleandir(dir = "$output/classes/compiler")
        javac2(destdir = "$output/classes/compiler", debug = "true", debuglevel = "lines,vars,source", includeAntRuntime = "false", source = javaTarget.toString(), target = javaTarget.toString()) {
            withKotlin(externalannotations = externalAnnotationsPath)
            src(refid = "compilerSources.path")
            classpath(refid = "classpath")
        }
        packCompiler(jarfile = "$output/kotlin-compiler-before-shrink.jar")
        delete(file = "$kotlinHome/lib/kotlin-compiler.jar", failOnError = false)
        if (!shrink) {
            copy(file = "$output/kotlin-compiler-before-shrink.jar", tofile = "$kotlinHome/lib/kotlin-compiler.jar")
        } else {
            available(property = "rtjar", value = "$javaHome/lib/rt.jar", file = "$javaHome/lib/rt.jar")
            available(property = "rtjar", value = "$javaHome/../Classes/classes.jar", file = "$javaHome/../Classes/classes.jar")
            available(property = "jssejar", value = "$javaHome/lib/jsse.jar", file = "$javaHome/lib/jsse.jar")
            available(property = "jssejar", value = "$javaHome/../Classes/jsse.jar", file = "$javaHome/../Classes/jsse.jar")
            proguard {
                text {
                    """

                    -injars '$output/kotlin-compiler-before-shrink.jar'(
                    !com/thoughtworks/xstream/converters/extended/ISO8601**,
                    !com/thoughtworks/xstream/converters/reflection/CGLIBEnhancedConverter**,
                    !com/thoughtworks/xstream/io/xml/Dom4J**,
                    !com/thoughtworks/xstream/io/xml/Xom**,
                    !com/thoughtworks/xstream/io/xml/Wstx**,
                    !com/thoughtworks/xstream/io/xml/KXml2**,
                    !com/thoughtworks/xstream/io/xml/BEAStax**,
                    !com/thoughtworks/xstream/io/json/Jettison**,
                    !com/thoughtworks/xstream/mapper/CGLIBMapper**,
                    !org/apache/log4j/jmx/Agent*,
                    !org/apache/log4j/net/JMS*,
                    !org/apache/log4j/net/SMTP*,
                    !org/apache/log4j/or/jms/MessageRenderer*,
                    !org/jdom/xpath/Jaxen*,
                    !org/mozilla/javascript/xml/impl/xmlbeans/**,
                    !META-INF/maven**,
                    **.class,**.properties,**.kt,**.kotlin_*,
                    META-INF/services/**,META-INF/native/**,META-INF/MANIFEST.MF,
                    messages/**)

                    -outjars '$kotlinHome/lib/kotlin-compiler.jar'

                    -dontnote **
                    -dontwarn com.intellij.util.ui.IsRetina*
                    -dontwarn com.intellij.util.RetinaImage*
                    -dontwarn apple.awt.*
                    -dontwarn dk.brics.automaton.*
                    -dontwarn org.fusesource.**
                    -dontwarn org.xerial.snappy.SnappyBundleActivator
                    -dontwarn com.intellij.util.CompressionUtil
                    -dontwarn com.intellij.util.SnappyInitializer
                    -dontwarn net.sf.cglib.**
                    -dontwarn org.objectweb.asm.** # this is ASM3, the old version that we do not use

                    -libraryjars '$rtjar'
                    -libraryjars '$jssejar'
                    -libraryjars '$bootstrapRuntime'

                    -target 1.6
                    -dontoptimize
                    -dontobfuscate

                    -keep class org.fusesource.** { *; }
                    -keep class org.jdom.input.JAXPParserFactory { *; }

                    -keep class org.jetbrains.annotations.** {
                        public protected *;
                    }

                    -keep class javax.inject.** {
                        public protected *;
                    }

                    -keep class org.jetbrains.k2js.** {
                        public protected *;
                    }

                    -keep class org.jetbrains.jet.** {
                        public protected *;
                    }

                    -keep class jet.** {
                        public protected *;
                    }

                    -keep class kotlin.** {
                        public protected *;
                    }

                    -keep class com.intellij.psi.** {
                        public protected *;
                    }

                    # for kdoc
                    -keep class com.intellij.openapi.util.TextRange { *; }

                    -keepclassmembers enum * {
                        public static **[] values();
                        public static ** valueOf(java.lang.String);
                    }

                    -keepclassmembers class * {
                        ** toString();
                        ** hashCode();
                        void start();
                        void stop();
                        void dispose();
                    }

                    -keepclassmembers class org.jetbrains.org.objectweb.asm.Opcodes {
                        *** ASM5;
                    }

                    -keepclassmembers class org.jetbrains.org.objectweb.asm.ClassReader {
                        *** SKIP_CODE;
                        *** SKIP_DEBUG;
                        *** SKIP_FRAMES;
                    }
                
                    """
                }
            }
        }
        jar(jarfile = "$output/kotlin-compiler-for-maven.jar") {
            zipfileset(src = "$kotlinHome/lib/kotlin-compiler.jar", includes = "**")
            zipfileset(src = bootstrapRuntime, includes = "**", excludes = "META-INF/**")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinCompiler)
                attribute(name = "Implementation-Version", value = buildNumber)
                attribute(name = "Main-Class", value = "org.jetbrains.jet.cli.jvm.K2JVMCompiler")
            }
        }
    }

    val antTools = target {
        cleandir(dir = "$output/classes/buildTools")
        javac2(destdir = "$output/classes/buildTools", debug = "true", debuglevel = "lines,vars,source", includeAntRuntime = "false", source = javaTarget.toString(), target = javaTarget.toString()) {
            withKotlin(externalannotations = externalAnnotationsPath)
            src {
                dirset(dir = "$basedir/build-tools") {
                    include(name = "core/src")
                    include(name = "ant/src")
                }
            }
            compilerarg(value = "-Xlint:all")
            classpath {
                fileset(dir = "$kotlinHome/lib", includes = "kotlin-compiler.jar")
                file(file = bootstrapRuntime)
                fileset(dir = "$dependenciesDir/ant-1.7/lib", includes = "ant.jar")
            }
        }
        jar(destFile = "$kotlinHome/lib/kotlin-ant.jar") {
            fileset(dir = "$output/classes/buildTools")
            fileset(dir = "$basedir/build-tools/ant/src", includes = "**/*.xml")
            zipfileset(file = "$kotlinHome/build.txt", prefix = "META-INF")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinCompilerAntTask)
                attribute(name = "Implementation-Version", value = buildNumber)
                attribute(name = "Class-Path", value = "kotlin-compiler.jar")
            }
        }
    }

    val jdkAnnotations = target {
        copy(file = "dependencies/annotations/kotlin-jdk-annotations.jar", todir = "$kotlinHome/lib")
    }

    val androidSdkAnnotations = target {
        copy(file = "dependencies/annotations/kotlin-android-sdk-annotations.jar", todir = "$kotlinHome/lib")
    }

    val runtime = target {
        newKotlinc(src = "$basedir/core/builtins/src $basedir/core/runtime.jvm/src $basedir/core/reflection/src", output = "$output/classes/runtime", classpath = "$basedir/core/runtime.jvm/src")
        javac2(destdir = "$output/classes/runtime", debug = "true", debuglevel = "lines,vars,source", includeAntRuntime = "false", source = javaTarget.toString(), target = javaTarget.toString()) {
            src(path = "$basedir/core/runtime.jvm/src")
            classpath(location = "$output/classes/runtime")
        }
        newKotlinc(src = "$basedir/libraries/stdlib/src", output = "$output/classes/stdlib", classpath = "$output/classes/runtime")
        jar(destFile = "$kotlinHome/lib/kotlin-runtime.jar") {
            fileset(dir = "$output/classes/runtime")
            fileset(dir = "$output/classes/stdlib")
            zipfileset(file = "$kotlinHome/build.txt", prefix = "META-INF")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinJvmRuntime)
                attribute(name = "Implementation-Version", value = buildNumber)
            }
        }
    }

    val runtimeSources = target("runtime_sources") {
        jar(destFile = "$kotlinHome/lib/kotlin-runtime-sources.jar") {
            fileset(dir = "$basedir/core/builtins/native", includes = "**/*")
            fileset(dir = "$basedir/core/builtins/src", includes = "**/*")
            fileset(dir = "$basedir/core/runtime.jvm/src", includes = "**/*")
            fileset(dir = "$basedir/core/reflection/src", includes = "**/*")
            fileset(dir = "$basedir/libraries/stdlib/src", includes = "**/*")
            zipfileset(file = "$kotlinHome/build.txt", prefix = "META-INF")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Title", value = manifestImplTitleKotlinJvmRuntimeSources)
                attribute(name = "Implementation-Version", value = buildNumber)
            }
        }
    }

    [default]
    val dist = target(::clean, ::init, ::prepareDist, ::preloader, ::builtins, ::compiler, ::compilerSources, ::antTools, ::jdkAnnotations, ::androidSdkAnnotations, ::runtime, ::runtimeSources, ::jslib) {}

    val distQuick = target("dist_quick", ::clean, ::init, ::prepareDist, ::preloader, ::builtins, ::compilerQuick, ::antTools, ::jdkAnnotations, ::androidSdkAnnotations, ::runtime, ::runtimeSources, ::jslib) {}

    val distQuickCompilerOnly = target("dist_quick_compiler_only", ::init, ::prepareDist, ::preloader, ::builtins, ::compilerQuick) {}

    val zip = target(::dist) {
        zip(destFile = "$output/$outputName.zip") {
            zipfileset(prefix = "kotlinc", dir = kotlinHome, excludes = "bin/*")
            zipfileset(prefix = "kotlinc/bin", dir = "$kotlinHome/bin", includes = "*.bat", fileMode = "644")
            zipfileset(prefix = "kotlinc/bin", dir = "$kotlinHome/bin", excludes = "*.bat", fileMode = "755")
        }
    }

    val kotlinForUpsource = target("kotlin-for-upsource", ::dist) {
        cleandir(dir = "$output/classes/idea-analysis")
        javac2(destdir = "$output/classes/idea-analysis", debug = "true", debuglevel = "lines,vars,source", includeAntRuntime = "false", source = javaTarget.toString(), target = javaTarget.toString()) {
            withKotlin(externalannotations = externalAnnotationsPath)
            src {
                dirset(dir = "$basedir/idea/idea-analysis") {
                    include(name = "src")
                }
            }
            classpath {
                fileset(dir = ideaSdk, includes = "core/*.jar")
                fileset(dir = ideaSdk, includes = "core-analysis/*.jar")
                fileset(dir = ideaSdk, includes = "lib/protobuf-2.5.0.jar")
                fileset(dir = "$basedir/lib", includes = "**/*.jar")
                fileset(dir = dependenciesDir, includes = "jline.jar")
                fileset(dir = dependenciesDir, includes = "jansi.jar")
                fileset(dir = dependenciesDir, includes = "cli-parser-1.1.1.jar")
                fileset(dir = "$basedir/ideaSDK/jps", includes = "jps-model.jar")
                pathelement(location = "$output/classes/runtime")
                pathelement(location = "$output/classes/compiler")
                pathelement(location = "$output/classes/stdlib")
            }
        }
        copy(todir = "$output/classes/idea-analysis") {
            fileset(dir = "$basedir/idea/idea-analysis/src", excludes = "**/*.java, **/*.kt")
        }
        jar(jarfile = "$output/kotlin-for-upsource0.jar") {
            fileset(dir = "$output/classes/idea-analysis")
            fileset(dir = "$output/classes/compiler")
            fileset(dir = "$output/classes/runtime")
            fileset(dir = "$output/builtins") {
                include(name = "kotlin/**")
                exclude(name = "kotlin/internal/**")
            }
            fileset(dir = "$basedir/compiler/frontend.java/src", includes = "META-INF/services/**")
            fileset(dir = "$basedir/compiler/backend/src", includes = "META-INF/services/**")
            zipGroupFileset(dir = "$basedir/lib", includes = "*.jar")
            zipGroupFileset(dir = "$kotlinHome/lib", includes = "kotlin-runtime-sources.jar")
            fileset(dir = "$output/classes/stdlib")
            fileset(dir = "idea/resources")
            fileset(dir = "idea/src", includes = "META-INF/**")
        }
        sleep(seconds = 1)
        jar(jarfile = "$output/kotlin-for-upsource.jar") {
            zipfileset(src = "$output/kotlin-for-upsource0.jar") {
                exclude(name = "javax/**/*.java")
            }
        }
        delete(file = "$output/kotlin-for-upsource0.jar")
        jar(jarfile = "$output/kotlin-for-upsource-sources.jar") {
            fileset(dir = "compiler/frontend/src")
            fileset(dir = "core/descriptors/src")
            fileset(dir = "core/descriptor.loader.java/src")
            fileset(dir = "core/serialization/src")
            fileset(dir = "core/serialization.java/src")
            fileset(dir = "compiler/frontend.java/src")
            fileset(dir = "compiler/backend-common/src")
            fileset(dir = "compiler/backend/src")
            fileset(dir = "compiler/cli/src")
            fileset(dir = "j2k/src")
            fileset(dir = "compiler/util/src")
            fileset(dir = "core/util.runtime/src")
            fileset(dir = "compiler/jet.as.java.psi/src")
            fileset(dir = "compiler/builtins-serializer")
            fileset(dir = "js/js.dart-ast/src")
            fileset(dir = "js/js.translator/src")
            fileset(dir = "js/js.frontend/src")
            fileset(dir = "idea/idea-analysis/src")
            manifest {
                attribute(name = "Built-By", value = manifestImplVendor)
                attribute(name = "Implementation-Vendor", value = manifestImplVendor)
                attribute(name = "Implementation-Version", value = buildNumber)
            }
        }
    }

    val buildArtifacts = target("build-artifacts", ::zip, ::kotlinForUpsource) {}
}
