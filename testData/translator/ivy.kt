import jetbrains.kant.dsl.types.selectors.contains
import jetbrains.kant.dsl.target
import jetbrains.kant.dsl.StringProperty
import jetbrains.kant.dsl.DSLProject
import jetbrains.kant.dsl.BooleanProperty
import jetbrains.kant.dsl.types.*
import jetbrains.kant.dsl.taskdefs.condition.equals
import jetbrains.kant.dsl.taskdefs.condition.isset
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.resources.javaresource

var eolNativeExcludes by StringProperty("eol.native.excludes")
var bundleVersion by StringProperty("bundle.version")
var findbugsRaw by StringProperty("findbugs.raw")
var skipJavadoc by BooleanProperty("skip.javadoc")
val libDir by StringProperty("lib.dir")
var ivyJarFile by StringProperty("ivy.jar.file")
val buildDir by StringProperty("build.dir")
val targetIvyBundleVersion by StringProperty("target.ivy.bundle.version")
var findbugsReportdir by StringProperty("findbugs.reportdir")
var eclipseClasspathAbort by BooleanProperty("eclipse.classpath.abort")
val userHome by StringProperty("user.home")
var checkstyleBasedir by StringProperty("checkstyle.basedir")
val classesBuildDir by StringProperty("classes.build.dir")
val reportsDir by StringProperty("reports.dir")
var ivyHome by StringProperty("ivy.home")
val artifactsBuildDir by StringProperty("artifacts.build.dir")
var noResolve by BooleanProperty("no.resolve")
val lineSeparator by StringProperty("line.separator")
val testDir by StringProperty("test.dir")
val pubdate by StringProperty()
var ivyUseLocalHome by BooleanProperty("ivy.use.local.home")
val exampleDir by StringProperty("example.dir")
val ivyMinimumJavaversion by StringProperty("ivy.minimum.javaversion")
val testClassPattern by StringProperty("test.class.pattern")
val eclipseClasspathConfirm by StringProperty("eclipse.classpath.confirm")
val bootstrapClassesBuildDir by StringProperty("bootstrap.classes.build.dir")
val antClassesBuildDir by StringProperty("ant.classes.build.dir")
var findbugsDownloadUrl by StringProperty("findbugs.download.url")
val projectVersion by StringProperty("project.version")
var skipTest by BooleanProperty("skip.test")
val debugMode by StringProperty("debug.mode")
var findbugsXsl by StringProperty("findbugs.xsl")
val basedir by StringProperty()
var findbugsHome by StringProperty("findbugs.home")
var offline by BooleanProperty()
val testBuildDir by StringProperty("test.build.dir")
val testXmlDir by StringProperty("test.xml.dir")
val testReportDir by StringProperty("test.report.dir")
val allClassesBuildDir by StringProperty("all.classes.build.dir")
var antHome by StringProperty("ant.home")
val coreClassesBuildDir by StringProperty("core.classes.build.dir")
var findbugsDownloadFile by StringProperty("findbugs.download.file")
var coverageClassesDir by StringProperty("coverage.classes.dir")
val docBuildDir by StringProperty("doc.build.dir")
val checkstyleReportDir by StringProperty("checkstyle.report.dir")
var findbugsDownloadTo by StringProperty("findbugs.download.to")
val finalName by StringProperty("final.name") { "ivy.jar" }
var emmaEnabled by BooleanProperty("emma.enabled")
var findbugsJvmargs by StringProperty("findbugs.jvmargs")
val coverageReportDir by StringProperty("coverage.report.dir")
val javadocBuildDir by StringProperty("javadoc.build.dir")
val envANTHOME by StringProperty("env.ANT_HOME")
val Name by StringProperty()
var coverageDir by StringProperty("coverage.dir")
val ivyReportDir by StringProperty("ivy.report.dir")
var buildVersion by StringProperty("build.version")
val targetIvyBundleVersionQualifier by StringProperty("target.ivy.bundle.version.qualifier")
var eolNativeIncludes by StringProperty("eol.native.includes")
val optionalClassesBuildDir by StringProperty("optional.classes.build.dir")
var versionPrefix by StringProperty("version.prefix")
val checkstyleSrcDir by StringProperty("checkstyle.src.dir")
var findbugsDownloadName by StringProperty("findbugs.download.name")
val srcDir by StringProperty("src.dir")
val targetIvyVersion by StringProperty("target.ivy.version")
val envIVYHOME by StringProperty("env.IVY_HOME")

object project : DSLProject() {
    {
        default = ::coverageReport
        property(environment = "env")
        property(file = "version.properties")
        property(file = "build.properties")
        available(file = "$basedir/.classpath", property = "eclipse.classpath.exists")
    }

    val initIvyUserHome = target("init-ivy-user-home") {
        if (isset(property = "env.IVY_HOME")) {
            ivyHome = envIVYHOME
        }
        ivyHome = "$userHome/.ivy2"
    }

    val initIvyLocalHome = target("init-ivy-local-home") {
        ivyHome = "$basedir/.ivy2"
    }

    val initIvyHome = target("init-ivy-home", ::initIvyUserHome, ::initIvyLocalHome) {}

    val initIvy = target("init-ivy", ::compileBootstrap, ::initIvyHome) {
        taskdef(resource = "org/apache/ivy/ant/antlib.xml", uRI = "antlib:org.apache.ivy.ant") {
            classpath {
                pathElement(location = coreClassesBuildDir)
                pathElement(location = bootstrapClassesBuildDir)
            }
        }
        ivy:configure(override = "true")
    }

    val install = target("install", ::initIvyHome, ::jar) {
        ivyJarFile = "$ivyHome/jars/ivy.jar"
        copy(file = "$artifactsBuildDir/jars/$finalName", tofile = ivyJarFile)
    }

    val installAnt = target("install-ant", ::initIvyHome, ::jar) {
        if (isset(property = "env.ANT_HOME")) {
            antHome = envANTHOME
        }
        fail(unless = "ant.home", message = "ANT_HOME environment variable or ant.home property required")
        copy(file = "$artifactsBuildDir/jars/$finalName", tofile = "$antHome/lib/ivy.jar")
    }

    val init = target("init") {
        val libClasspath = path {
            fileset(dir = libDir) {
                include(name = "*.jar")
            }
        }
        val buildBootstrapClasspath = path {
            pathElement(location = coreClassesBuildDir)
        }
        val buildAntClasspath = path {
            pathElement(location = coreClassesBuildDir)
            path(refid = libClasspath)
        }
        val buildOptionalClasspath = path {
            path(refid = buildAntClasspath)
        }
        val runClasspath = path {
            pathElement(location = coreClassesBuildDir)
            pathElement(location = antClassesBuildDir)
            pathElement(location = optionalClassesBuildDir)
            path(refid = libClasspath)
        }
        val testClasspath = path {
            pathElement(location = coverageClassesDir)
            fileset(dir = libDir) {
                include(name = "*.jar")
                exclude(name = "ant.jar")
                exclude(name = "ant-launcher.jar")
                exclude(name = "ant-nodeps.jar")
                exclude(name = "ant-trax.jar")
            }
            pathElement(location = coreClassesBuildDir)
            pathElement(location = antClassesBuildDir)
            pathElement(location = optionalClassesBuildDir)
            pathElement(path = testBuildDir)
        }
    }

    val prepare = target("prepare", ::init) {
        mkdir(dir = classesBuildDir)
        mkdir(dir = coreClassesBuildDir)
        mkdir(dir = bootstrapClassesBuildDir)
        mkdir(dir = antClassesBuildDir)
        mkdir(dir = optionalClassesBuildDir)
        mkdir(dir = allClassesBuildDir)
        mkdir(dir = testBuildDir)
        mkdir(dir = artifactsBuildDir)
        mkdir(dir = testReportDir)
        mkdir(dir = ivyReportDir)
    }

    val clean = target("clean") {
        delete(dir = classesBuildDir)
        delete(dir = testBuildDir)
        delete(dir = artifactsBuildDir)
        delete(dir = testReportDir)
        delete(dir = javadocBuildDir)
        delete(dir = docBuildDir)
        delete(dir = buildDir)
    }

    val cleanLib = target("clean-lib") {
        delete(dir = libDir)
    }

    val cleanIvyCache = target("clean-ivy-cache", ::initIvyHome) {
        delete(dir = "$ivyHome/cache")
    }

    val cleanIvyHome = target("clean-ivy-home", ::initIvyHome) {
        delete(dir = ivyHome)
    }

    val cleanExamples = target("clean-examples") {
        subant(target = "clean", failonerror = false) {
            fileset(dir = exampleDir, includes = "**/build.xml")
        }
    }

    val cleanAll = target("clean-all", ::clean, ::cleanLib, ::cleanExamples) {}

    val /noresolve = target("/noresolve") {
        noResolve = true
    }

    val /notest = target("/notest") {
        skipTest = true
    }

    val /nojavadoc = target("/nojavadoc") {
        skipJavadoc = true
    }

    val /localivy = target("/localivy") {
        ivyUseLocalHome = true
    }

    val /offline = target("/offline", ::/noresolve) {
        offline = true
    }

    val defaultVersion = target("default-version") {
        tstamp {
            format(property = "pubdate", pattern = "yyyyMMddHHmmss")
        }
        versionPrefix = "$targetIvyVersion-local-"
        buildVersion = "${versionPrefix}$pubdate"
        bundleVersion = "$targetIvyBundleVersion.${targetIvyBundleVersionQualifier}$pubdate"
    }

    val resolve = target("resolve", ::initIvy) {
        ivy:retrieve(conf = "default,test", pattern = "$libDir/[artifact].[ext]", sync = "yes")
    }

    val compileCore = target("compile-core", ::prepare) {
        javac(srcdir = srcDir, destdir = coreClassesBuildDir, sourcepath = "", source = ivyMinimumJavaversion, target = ivyMinimumJavaversion, debug = debugMode.toBoolean(), includeantruntime = false) {
            excludesFile(name = "ant.patterns")
            excludesFile(name = "optional.patterns")
        }
        copy(todir = coreClassesBuildDir, includeEmptyDirs = false) {
            fileset(dir = srcDir) {
                exclude(name = "**/*.java")
                excludesFile(name = "ant.patterns")
                excludesFile(name = "optional.patterns")
            }
        }
        copy(file = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivysettings-local.xml", tofile = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivyconf-local.xml")
        copy(file = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivysettings-default-chain.xml", tofile = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivyconf-default-chain.xml")
        copy(file = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivysettings-main-chain.xml", tofile = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivyconf-main-chain.xml")
        copy(file = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivysettings-public.xml", tofile = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivyconf-public.xml")
        copy(file = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivysettings-shared.xml", tofile = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivyconf-shared.xml")
        copy(file = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivysettings.xml", tofile = "$coreClassesBuildDir/org/apache/ivy/core/settings/ivyconf.xml")
    }

    val compileBootstrap = target("compile-bootstrap", ::compileCore) {
        javac(srcdir = srcDir, destdir = bootstrapClassesBuildDir, sourcepath = "", classpathRef = buildBootstrapClasspath, source = ivyMinimumJavaversion, target = ivyMinimumJavaversion, debug = debugMode.toBoolean(), includeantruntime = true) {
            includesFile(name = "ant.patterns")
        }
        copy(todir = bootstrapClassesBuildDir, includeEmptyDirs = false) {
            fileset(dir = srcDir) {
                includesFile(name = "ant.patterns")
                exclude(name = "**/*.java")
            }
        }
    }

    val compileAnt = target("compile-ant", ::compileCore, ::resolve) {
        javac(srcdir = srcDir, destdir = antClassesBuildDir, sourcepath = "", classpathRef = buildAntClasspath, source = ivyMinimumJavaversion, target = ivyMinimumJavaversion, debug = debugMode.toBoolean(), includeantruntime = false) {
            includesFile(name = "ant.patterns")
        }
        copy(todir = antClassesBuildDir, includeEmptyDirs = false) {
            fileset(dir = srcDir) {
                includesFile(name = "ant.patterns")
                exclude(name = "**/*.java")
            }
        }
        copy(file = "$antClassesBuildDir/org/apache/ivy/ant/antlib.xml", todir = "$antClassesBuildDir/fr/jayasoft/ivy/ant")
    }

    val compileOptional = target("compile-optional", ::compileAnt, ::resolve) {
        javac(srcdir = srcDir, destdir = optionalClassesBuildDir, sourcepath = "", classpathRef = buildOptionalClasspath, source = ivyMinimumJavaversion, target = ivyMinimumJavaversion, debug = debugMode.toBoolean(), includeantruntime = false) {
            includesFile(name = "optional.patterns")
            includesFile(name = "ant.patterns")
        }
        copy(todir = coreClassesBuildDir, includeEmptyDirs = false) {
            fileset(dir = srcDir) {
                includesFile(name = "optional.patterns")
                exclude(name = "**/*.java")
            }
        }
    }

    val jar = target("jar", ::compileOptional, ::defaultVersion) {
        echo(message = "version=${buildVersion}$lineSeparator", file = "$coreClassesBuildDir/module.properties", append = true)
        echo(message = "date=${pubdate}$lineSeparator", file = "$coreClassesBuildDir/module.properties", append = true)
        mkdir(dir = "$artifactsBuildDir/jars/")
        copy(file = "$basedir/META-INF/MANIFEST.MF", tofile = "$artifactsBuildDir/MANIFEST.MF") {
            filterChain {
                replaceRegex(pattern = "Bundle-Version:.*", replace = "Bundle-Version: $bundleVersion", byLine = true)
            }
        }
        copy(todir = allClassesBuildDir) {
            fileset(dir = coreClassesBuildDir)
            fileset(dir = antClassesBuildDir)
            fileset(dir = optionalClassesBuildDir)
        }
        jar(destFile = "$artifactsBuildDir/jars/$finalName", manifest = "$artifactsBuildDir/MANIFEST.MF") {
            metainf(dir = basedir, includes = "LICENSE,NOTICE")
            manifest {
                attribute(name = "Specification-Title", value = "Apache Ivy with Ant tasks")
                attribute(name = "Specification-Version", value = buildVersion)
                attribute(name = "Specification-Vendor", value = "Apache Software Foundation")
                attribute(name = "Implementation-Title", value = "org.apache.ivy")
                attribute(name = "Implementation-Version", value = buildVersion)
                attribute(name = "Implementation-Vendor", value = "Apache Software Foundation")
                attribute(name = "Implementation-Vendor-Id", value = "org.apache")
                attribute(name = "Extension-name", value = "org.apache.ivy")
                attribute(name = "Build-Version", value = buildVersion)
            }
            fileset(dir = allClassesBuildDir)
        }
        copy(file = "$artifactsBuildDir/jars/$finalName", tofile = "$artifactsBuildDir/org.apache.ivy_$bundleVersion.jar")
        delete(file = "$coreClassesBuildDir/module.properties")
    }

    val publishLocal = target("publish-local", ::jar, ::sources) {
        ivy:publish(resolver = "local", pubrevision = buildVersion, artifactsPattern = "$artifactsBuildDir/[type]s/[artifact].[ext]", forcedeliver = "true")
    }

    val buildCustomResolverJar = target("build-custom-resolver-jar", ::jar) {
        mkdir(dir = "$buildDir/custom-classpath")
        javac(srcdir = "$basedir/test/custom-classpath", destdir = "$buildDir/custom-classpath", classpathRef = runClasspath, source = ivyMinimumJavaversion, target = ivyMinimumJavaversion, debug = debugMode.toBoolean(), includeantruntime = false)
        jar(destFile = "$testDir/org/apache/ivy/core/settings/custom-resolver.jar", basedir = "$buildDir/custom-classpath")
    }

    val initTestsOffline = target("init-tests-offline") {
        val testFileset = fileset(dir = testDir) {
            include(name = "**/$testClassPattern.java")
            exclude(name = "**/Abstract*Test.java")
            not {
                contains(text = "remote.test")
            }
        }
    }

    val initTestsOnline = target("init-tests-online") {
        val testFileset = fileset(dir = testDir) {
            include(name = "**/$testClassPattern.java")
            exclude(name = "**/Abstract*Test.java")
        }
    }

    val initTests = target("init-tests", ::initTestsOffline, ::initTestsOnline) {}

    val emma = target("emma", ::jar) {
        ivy:cachepath(organisation = "emma", module = "emma", revision = "2.0.5312", inline = "true", conf = "default", pathid = "emma.classpath", log = "download-only")
        ivy:cachepath(organisation = "emma", module = "emma_ant", revision = "2.0.5312", inline = "true", conf = "default", pathid = "emma.ant.classpath", transitive = "false", log = "download-only")
        taskdef(resource = "emma_ant.properties") {
            classpath(refid = emmaClasspath)
            classpath(refid = emmaAntClasspath)
        }
        emmaEnabled = true
        coverageDir = "$buildDir/coverage"
        coverageClassesDir = "$coverageDir/classes"
        mkdir(dir = coverageDir)
        mkdir(dir = coverageClassesDir)
        emma(enabled = emmaEnabled.toString()) {
            instr(mode = "copy", destdir = "$coverageDir/classes", metadatafile = "$coverageDir/metadata.emma") {
                instrpath {
                    pathelement(location = coreClassesBuildDir)
                    pathelement(location = antClassesBuildDir)
                    pathelement(location = optionalClassesBuildDir)
                }
            }
        }
        delete(file = "$coverageDir/coverage.emma")
        ivy:addpath(topath = "test.classpath", first = "true") {
            pathelement(location = "$coverageDir/classes")
            path(refid = emmaClasspath)
        }
    }

    val buildTest = target("build-test", ::jar) {
        javac(srcdir = testDir, destdir = testBuildDir, classpathRef = runClasspath, source = ivyMinimumJavaversion, target = ivyMinimumJavaversion, debug = debugMode.toBoolean(), encoding = "ISO-8859-1", includeantruntime = false)
        copy(todir = testBuildDir) {
            fileset(dir = testDir) {
                exclude(name = "**/*.java")
            }
        }
    }

    val prepareOsgiTests = target("prepare-osgi-tests", ::resolve) {
        ant(dir = "$basedir/test/test-repo", target = "generate-bundles")
    }

    val prepareTestJarRepositories = target("prepare-test-jar-repositories") {
        mkdir(dir = "$basedir/test/jar-repos")
        jar(destFile = "$basedir/test/jar-repos/jarrepo1.jar") {
            fileset(dir = "$basedir/test/repositories/1")
        }
        jar(destFile = "$basedir/test/jar-repos/jarrepo1_subdir.jar") {
            fileset(dir = "$basedir/test/repositories", includes = "1/**/*")
        }
    }

    val testInternal = target("test-internal", ::buildTest, ::initTests, ::prepareOsgiTests, ::prepareTestJarRepositories) {
        mkdir(dir = testXmlDir)
        junit(haltonfailure = "off", haltonerror = "off", errorproperty = "test.failed", failureproperty = "test.failed", showoutput = "no", printsummary = "yes", includeantruntime = "yes", dir = basedir, fork = "true") {
            classpath {
                path(refid = testClasspath)
                pathelement(path = "$antHome/lib/ant-nodeps.jar")
                pathelement(path = "$antHome/lib/ant-trax.jar")
            }
            syspropertyset {
                propertyref(prefix = "http")
            }
            jvmarg(value = "-Demma.coverage.out.file=$coverageDir/coverage.emma")
            jvmarg(value = "-Demma.coverage.out.merge=true")
            jvmarg(value = "-Duser.region=TR")
            jvmarg(value = "-Duser.language=tr")
            formatter(`type` = "xml")
            batchtest(todir = testXmlDir) {
                fileset(refid = testFileset)
            }
        }
    }

    val test = target("test", ::testInternal) {
        fail(`if` = "test.failed", message = "At least one test has failed. See logs (in $testXmlDir) for details (use the target test-report to run the test with a report)")
    }

    val testReport = target("test-report", ::testInternal) {
        junitreport(todir = testXmlDir) {
            fileset(dir = testXmlDir) {
                include(name = "TEST-*.xml")
            }
            report(format = "frames", todir = testReportDir)
        }
        fail(`if` = "test.failed", message = "At least one test has failed. See logs (in $testXmlDir) or report (in $testReportDir)")
    }

    val coverageReport = target("coverage-report", ::emma, ::testReport) {
        mkdir(dir = coverageReportDir)
        emma {
            report(sourcepath = srcDir) {
                fileset(dir = coverageDir) {
                    include(name = "*.emma")
                }
                txt(outfile = "$coverageReportDir/coverage.txt")
                html(outfile = "$coverageReportDir/coverage.html")
            }
        }
    }

    val ivyReport = target("ivy-report", ::resolve) {
        ivy:report(todir = ivyReportDir)
    }

    val javadoc = target("javadoc") {
        javadoc(destdir = javadocBuildDir, useExternalFile = true) {
            fileset(dir = srcDir, includes = "**/*.java")
        }
    }

    val sources = target("sources", ::defaultVersion) {
        mkdir(dir = "$artifactsBuildDir/sources/")
        jar(destFile = "$artifactsBuildDir/sources/$finalName") {
            metainf(dir = basedir, includes = "LICENSE,NOTICE")
            manifest {
                attribute(name = "Specification-Title", value = "Apache Ivy Sources")
                attribute(name = "Specification-Version", value = buildVersion)
                attribute(name = "Specification-Vendor", value = "Apache Software Foundation")
            }
            fileset(dir = srcDir)
        }
    }

    val fixcrlf = target("fixcrlf") {
        eolNativeIncludes = "**/*.html,**/*.json,**/*.java,**/*.xml,**/*.txt,**/*.MF,**/*.properties,**/*.patterns,**/*.pom,**/*.xsl,**/*.css"
        eolNativeExcludes = "build/**,bin/**,lib/**"
        val eolNativeFileset = fileset(dir = basedir, includes = eolNativeIncludes, excludes = eolNativeExcludes)
        fixcrlf(srcdir = basedir, includes = eolNativeIncludes, excludes = eolNativeExcludes)
        apply(executable = "svn") {
            fileset(refid = eolNativeFileset)
            arg(value = "propset")
            arg(value = "svn:eol-style")
            arg(value = "\"native\"")
        }
    }

    val checkstyleInternal = target("checkstyle-internal", ::jar) {
        ivy:cachepath(organisation = "checkstyle", module = "checkstyle", revision = "5.0", inline = "true", conf = "default", pathid = "checkstyle.classpath", transitive = "true", log = "download-only")
        taskdef(resource = "checkstyletask.properties", classpathRef = checkstyleClasspath)
        mkdir(dir = checkstyleReportDir)
        checkstyle(config = "$checkstyleSrcDir/checkstyle-config", failOnViolation = "false", failureProperty = "checkstyle.failed") {
            classpath {
                path(refid = runClasspath)
            }
            formatter(`type` = "xml", toFile = "$checkstyleReportDir/checkstyle.xml")
            fileset(dir = srcDir) {
                include(name = "**/*.java")
            }
            fileset(dir = exampleDir) {
                include(name = "**/*.java")
            }
        }
    }

    val checkstyle = target("checkstyle", ::checkstyleInternal) {
        fail(`if` = "checkstyle.failed", message = "Checkstyle has errors. See report in $checkstyleReportDir")
    }

    val checkstyleReport = target("checkstyle-report", ::checkstyleInternal) {
        property(name = "checkstyle.basedir", location = srcDir)
        xslt(`in` = "$checkstyleReportDir/checkstyle.xml", style = "$checkstyleSrcDir/checkstyle-frames.xsl", out = "$checkstyleReportDir/output.txt") {
            param(name = "basedir", expression = checkstyleBasedir)
        }
    }

    val initFindbugs = target("init-findbugs") {
        property(name = "findbugs.download.name", value = "findbugs-1.3.5", description = "Name of the download file without suffix. Also the internal root directory of the ZIP.")
        property(name = "findbugs.download.file", value = "$findbugsDownloadName.zip", description = "The filename of the ZIP.")
        property(name = "findbugs.download.url", value = "http://garr.dl.sourceforge.net/sourceforge/findbugs/$findbugsDownloadFile", description = "The download adress at a mirror of Sourceforge.")
        property(name = "findbugs.download.to", value = "$buildDir/.downloads", description = "Where to store the download and 'install' Findbugs.")
        available(property = "findbugs.home", value = "$findbugsDownloadTo/$findbugsDownloadName", file = "$findbugsDownloadTo/$findbugsDownloadName/lib/findbugs.jar", description = "Check if Findbugs is already installed.")
        property(name = "findbugs.reportdir", location = "$reportsDir/findbugs", description = "Where to store Findbugs results")
        property(name = "findbugs.raw", value = "raw.xml", description = "Findbugs Output xml-file")
        property(name = "findbugs.xsl", value = "fancy.xsl", description = "Which XSL to use for generating Output: default, fancy, plain, summary")
        property(name = "findbugs.jvmargs", value = "-Xms128m -Xmx512m", description = "JVMArgs for invoking Findbugs")
        mkdir(dir = findbugsDownloadTo)
        get(src = findbugsDownloadUrl, dest = "$findbugsDownloadTo/$findbugsDownloadFile")
        unzip(src = "$findbugsDownloadTo/$findbugsDownloadFile", dest = findbugsDownloadTo)
        property(name = "findbugs.home", location = "$findbugsDownloadTo/$findbugsDownloadName")
        mkdir(dir = "$findbugsHome/plugin")
    }

    val findbugs = target("findbugs", ::initFindbugs, ::compileCore) {
        val findbugsRealClasspath = path {
            fileset(dir = "$findbugsHome/lib", includes = "*.jar")
        }
        taskdef(uRI = "http://findbugs.sourceforge.net/", resource = "edu/umd/cs/findbugs/anttask/tasks.properties", classpathRef = findbugsRealClasspath)
        mkdir(dir = findbugsReportdir)
        fb:findbugs(home = findbugsHome, classpathref = "findbugs.real.classpath", output = "xml:withMessages", outputFile = "$findbugsReportdir/$findbugsRaw", jvmargs = findbugsJvmargs, projectName = "$Name $projectVersion") {
            `class`(location = coreClassesBuildDir)
            sourcePath(path = srcDir)
        }
        xslt(basedir = findbugsReportdir, includes = findbugsRaw, destdir = findbugsReportdir) {
            style {
                javaresource(name = findbugsXsl, classpathRef = findbugsRealClasspath)
            }
        }
    }

    val checkEclipseClasspathOverwrite = target("check-eclipse-classpath-overwrite") {
        input(message = ".classpath file already exists.${lineSeparator}Are you sure you want to overwrite it and loose your original file?", validargs = "Y,N,y,n", addproperty = "eclipse.classpath.confirm")
        if (equals(arg1 = eclipseClasspathConfirm, arg2 = "N", casesensitive = false)) {
            eclipseClasspathAbort = true
        }
    }

    val eclipseDefault = target("eclipse-default", ::resolve, ::checkEclipseClasspathOverwrite) {
        copy(file = "$basedir/.classpath.default", tofile = "$basedir/.classpath", overwrite = true)
    }

    val eclipseIvyde = target("eclipse-ivyde", ::checkEclipseClasspathOverwrite) {
        copy(file = "$basedir/.classpath.ivyde", tofile = "$basedir/.classpath", overwrite = true)
    }
}
