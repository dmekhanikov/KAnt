import jetbrains.kant.dsl.types.selectors.*
import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.types.*
import jetbrains.kant.dsl.taskdefs.condition.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.resources.resources
import jetbrains.kant.dsl.types.resources.file
import jetbrains.kant.dsl.types.resources.intersect
import jetbrains.kant.dsl.filters.replaceRegex
import jetbrains.kant.dsl.filters.contains

val javaVmVersion by StringProperty("java.vm.version")
val utilPackage by StringProperty("util.package") { "$antPackage/util" }
val chmodMaxparallel by IntProperty("chmod.maxparallel") { 250 }
var antJunitFailureCollector by StringProperty("ant.junit.failureCollector")
val buildTests by StringProperty("build.tests") { "$buildDir/testcases" }
val buildDir by StringProperty("build.dir") { "build" }
val antunitReports by StringProperty("antunit.reports")
var distDirResolved by StringProperty("dist.dir.resolved")
var antunitIncludes by StringProperty("antunit.includes")
val groupid by StringProperty() { "org/apache/ant" }
val javaDir by StringProperty("java.dir") { "$srcDir/main" }
val testsEtcDir by StringProperty("tests.etc.dir") { "$srcDir/etc/testcases" }
var distName by StringProperty("dist.name")
var antunitTestcase by StringProperty("antunit.testcase")
var xmlschemaPresent by BooleanProperty("xmlschema.present")
var javamailComplete by BooleanProperty("javamail.complete")
val antunitLoglevel by StringProperty("antunit.loglevel") { "none" }
var distDir by StringProperty("dist.dir")
val buildLib by StringProperty("build.lib") { "$buildDir/lib" }
val srcAntunit by StringProperty("src.antunit") { "$srcDir/tests/antunit" }
val junitFork by BooleanProperty("junit.fork") { true }
var runAntunit by BooleanProperty("run.antunit")
val javaVmVendor by StringProperty("java.vm.vendor")
val buildTestsValue by StringProperty("build.tests.value")
val buildClasses by StringProperty("build.classes") { "$buildDir/classes" }
var junitExcludes by StringProperty("junit.excludes")
val javaClassPath by StringProperty("java.class.path")
val buildJunitTmpdir by StringProperty("build.junit.tmpdir")
var wixHome by StringProperty("wix.home")
val osArch by StringProperty("os.arch")
var junitSingle by BooleanProperty("junit.single")
val buildLibSrc by StringProperty("build.lib-src") { "$buildDir/lib-src" }
val regexpPackage by StringProperty("regexp.package") { "$utilPackage/regexp" }
val manifest by StringProperty() { "$srcDir/etc/manifest" }
val deprecation by BooleanProperty() { false }
val debug by BooleanProperty() { true }
var distBin by StringProperty("dist.bin")
var offline by StringProperty()
val optionalJarsWhenmanifestonly by StringProperty("optional.jars.whenmanifestonly") { "skip" }
val antHome by StringProperty("ant.home")
var expandpropertyFiles by StringProperty("expandproperty.files")
var testsAreOnSystemClasspath by BooleanProperty("tests.are.on.system.classpath")
val scriptDir by StringProperty("script.dir") { "$srcDir/script" }
var swingPresent by BooleanProperty("swing.present")
val junitForkmode by StringProperty("junit.forkmode") { "once" }
var testsFailed by BooleanProperty("tests.failed")
val buildTestsJavadocs by StringProperty("build.tests.javadocs") { "$buildDir/javadocs.test/" }
val antVersion by StringProperty("ant.version")
val osName by StringProperty("os.name")
val junitFiltertrace by StringProperty("junit.filtertrace") { "off" }
var distLib by StringProperty("dist.lib")
val bootstrapDir by StringProperty("bootstrap.dir")
val osVersion by StringProperty("os.version")
val optionalJarsPrefix by StringProperty("optional.jars.prefix") { "ant" }
var msiDir by StringProperty("msi.dir")
var distEtc by StringProperty("dist.etc")
val bootstrapJar by StringProperty("bootstrap.jar") { "ant-bootstrap.jar" }
val chmodFail by BooleanProperty("chmod.fail") { true }
var runJunit by BooleanProperty("run.junit")
val javaVmInfo by StringProperty("java.vm.info")
val libDir by StringProperty("lib.dir") { "lib" }
var junitBatch by BooleanProperty("junit.batch")
var testJunitVmargs by StringProperty("test.junit.vmargs")
var distJavadocs by StringProperty("dist.javadocs")
val pomVersion by StringProperty("pom.version") { "1.9.5-SNAPSHOT" }
val userHome by StringProperty("user.home")
val javacTarget by DoubleProperty("javac.target") { 1.5 }
val antInstall by StringProperty("ant.install")
val javaVersion by StringProperty("java.version")
val optionalPackage by StringProperty("optional.package") { "$taskdefsPackage/optional" }
val fileEncoding by StringProperty("file.encoding")
val antunitXml by StringProperty("antunit.xml")
var javadocDoclintNone by StringProperty("javadoc.doclint.none")
val buildJunitXml by StringProperty("build.junit.xml")
var buildosxpackage by BooleanProperty()
val buildJavadocs by StringProperty("build.javadocs") { "$buildDir/javadocs" }
val manifestVersion by StringProperty("manifest-version") { "1.9.5" }
var antunitRecommendedLocation by BooleanProperty("antunit.recommended.location")
var javaRepositoryDir by StringProperty("java-repository.dir")
var runAntunitReport by BooleanProperty("run.antunit.report")
val optimize by BooleanProperty() { true }
var junitCollectorClass by StringProperty("junit.collector.class")
var ignoresystemclasses by BooleanProperty()
val userLanguage by StringProperty("user.language")
val testHaltonfailure by BooleanProperty("test.haltonfailure") { false }
val buildSysclasspath by StringProperty("build.sysclasspath")
val javacSource by DoubleProperty("javac.source") { 1.5 }
var srcDistSrc by StringProperty("src.dist.src")
var antunitExcludes by StringProperty("antunit.excludes")
var junitTestcase by StringProperty("junit.testcase")
val srcJunit by StringProperty("src.junit") { "$srcDir/tests/junit" }
val taskdefsPackage by StringProperty("taskdefs.package") { "$antPackage/taskdefs" }
var buildCompiler by StringProperty("build.compiler")
val projectVersion by StringProperty("project.version") { "1.9.5alpha" }
val javaVmName by StringProperty("java.vm.name")
val javaHome by StringProperty("java.home")
val conditionPackage by StringProperty("condition.package") { "$taskdefsPackage/condition" }
val optionalTypePackage by StringProperty("optional.type.package") { "$typePackage/optional" }
var junitThreads by StringProperty("junit.threads")
val basedir by StringProperty()
var distBase by StringProperty("dist.base")
var srcDistManual by StringProperty("src.dist.manual")
val apacheResolverTypePackage by StringProperty("apache.resolver.type.package") { "$antPackage/types/resolver" }
val junitSummary by BooleanProperty("junit.summary") { false }
var junitCollectorDir by StringProperty("junit.collector.dir")
var srcDistDir by StringProperty("src.dist.dir")
var junitIncludes by StringProperty("junit.includes")
val typePackage by StringProperty("type.package") { "$antPackage/types" }
val name by StringProperty() { "ant" }
var distBaseBinaries by StringProperty("dist.base.binaries")
val toString:testsRuntimeClasspath by StringProperty("toString:tests-runtime-classpath")
var testsAndAntShareClassloader by StringProperty("tests.and.ant.share.classloader")
val manualDir by StringProperty("manual.dir") { "manual" }
var msiFile by StringProperty("msi.file")
val javadocVerbose by StringProperty("javadoc.verbose")
val resourceDir by StringProperty("resource.dir") { "$srcDir/resources" }
val buildJunitReports by StringProperty("build.junit.reports")
var apacheHttpclientPresent by BooleanProperty("apache-httpclient.present")
var distBaseSource by StringProperty("dist.base.source")
val manifestTmp by StringProperty("manifest.tmp") { "$buildDir/optional.manifest" }
var distBaseManual by StringProperty("dist.base.manual")
var srcDistLib by StringProperty("src.dist.lib")
val etcDir by StringProperty("etc.dir") { "$srcDir/etc" }
val testcase by StringProperty()
val antPackage by StringProperty("ant.package") { "org/apache/tools/ant" }
val antunitTmpdir by StringProperty("antunit.tmpdir")
val Name by StringProperty() { "Apache Ant" }
var jasperPresent by BooleanProperty("jasper.present")
var jdk19+ by BooleanProperty("jdk1.9+")
var distManual by StringProperty("dist.manual")
var sunToolsPresent by BooleanProperty("sun.tools.present")
var runJunitReport by BooleanProperty("run.junit.report")
var wixobjDir by StringProperty("wixobj.dir")
var failingtestsDir by StringProperty("failingtests.dir")
val srcDir by StringProperty("src.dir") { "src" }
var msiName by StringProperty("msi.name")

fun DSLTaskContainer.optionalJar(dep: String) {
    jar(destFile = "$buildLib/$optionalJarsPrefix-$dep.jar", basedir = buildClasses, manifest = manifestTmp, whenmanifestonly = optionalJarsWhenmanifestonly) {
        selector(refid = needs@{dep})
        metainf(dir = buildDir) {
            include(name = "LICENSE.txt")
            include(name = "NOTICE.txt")
        }
    }
}

fun DSLTaskContainer.optionalSrcJar(dep: String) {
    jar(destFile = "$buildLibSrc/$optionalJarsPrefix-$dep.jar", basedir = javaDir, whenmanifestonly = optionalJarsWhenmanifestonly) {
        selector(refid = needs@{dep})
        metainf(dir = buildDir) {
            include(name = "LICENSE.txt")
            include(name = "NOTICE.txt")
        }
    }
}

fun DSLTaskContainer.checksumsMvn() {
    element(name = "resources", implicit = "true")
    checksum(algorithm = "md5") {
        resources()
    }
    checksum(algorithm = "sha1") {
        resources()
    }
}

fun DSLTaskContainer.checksums() {
    element(name = "resources", implicit = "true")
    checksumsMvn {
        resources()
    }
    checksum(fileext = ".sha512", algorithm = "sha-512") {
        resources()
    }
}

fun DSLTaskContainer.testJunit() {
    element(name = "junit-nested", implicit = "true")
    delete(failOnError = false) {
        fileset(dir = junitCollectorDir, includes = "$junitCollectorClass*.class")
    }
    mkdir(dir = junitCollectorDir)
    available(file = "$junitCollectorDir/$junitCollectorClass.class", property = "hasFailingTests")
    mkdir(dir = buildJunitXml)
    testJunitVmargs = ""
    antJunitFailureCollector = "$junitCollectorDir/$junitCollectorClass"
    junit(printsummary = junitSummary.toString(), haltonfailure = testHaltonfailure.toString(), fork = junitFork.toString(), forkmode = junitForkmode, threads = junitThreads, failureproperty = "junit.failed", errorproperty = "junit.failed", filtertrace = junitFiltertrace) {
        sysproperty(key = "ant.home", value = antHome)
        sysproperty(key = "build.tests", file = buildTests)
        sysproperty(key = "build.tests.value", value = buildTestsValue)
        sysproperty(key = "offline", value = offline)
        sysproperty(key = "tests-classpath.value", value = toString:testsRuntimeClasspath)
        sysproperty(key = "root", file = basedir)
        sysproperty(key = "build.compiler", value = buildCompiler)
        sysproperty(key = "tests.and.ant.share.classloader", value = testsAndAntShareClassloader)
        sysproperty(key = "java.io.tmpdir", file = buildJunitTmpdir)
        classpath {
            path(refid = testsRuntimeClasspath)
            pathelement(location = junitCollectorDir)
        }
        formatter(`type` = "xml")
        jvmarg(line = testJunitVmargs)
        junitNested()
    }
}

object project : DSLProject() {
    {
        property(file = ".ant.properties")
        property(file = "$userHome/.ant.properties")
        property(environment = "env")
        if (equals(arg1 = junitFork.toString(), arg2 = "true") && equals(arg1 = junitForkmode, arg2 = "perTest")) {
            junitThreads = 2
        } else {
            junitThreads = 0
        }
        expandpropertyFiles = "**/version.txt,**/defaultManifest.mf"
        junitCollectorDir = "$buildDir/failingTests"
        junitCollectorClass = "FailedTests"
    }
    val classpath = path {
        fileset(dir = "lib/optional", includes = "*.jar")
    }
    val testsClasspath = path {
        pathElement(location = buildClasses)
        path(refid = classpath)
    }
    val testsRuntimeClasspath = path {
        path(refid = testsClasspath)
        pathElement(location = buildTests)
        pathElement(location = srcJunit)
        pathElement(location = testsEtcDir)
        pathElement(location = "$javaHome/../lib/tools.jar")
    };
    {
        distName = "apache-$name-$projectVersion"
        distBase = "distribution"
        distBaseSource = "$distBase/source"
        distBaseBinaries = "$distBase/binaries"
        distBaseManual = "$distBase/manual"
        property(name = "dist.dir", location = "dist")
        distBin = "$distDir/bin"
        distLib = "$distDir/lib"
        distManual = "$distDir/manual"
        distEtc = "$distDir/etc"
        distJavadocs = "$distDir/manual/api"
        srcDistDir = "dist-src"
        srcDistSrc = "$srcDistDir/src"
        srcDistManual = "$srcDistDir/manual"
        srcDistLib = "$srcDistDir/lib"
        javaRepositoryDir = "java-repository/$groupid"
    }
    val notInKaffe = selector {
        or {
            filename(name = "$conditionPackage/IsReachable*")
        }
    }
    val needsApacheResolver = selector {
        filename(name = "$apacheResolverTypePackage/")
    }
    val needsJunit = selector {
        and {
            filename(name = "$optionalPackage/junit/")
            not {
                or {
                    filename(name = "$optionalPackage/junit/JUnit4TestMethodAdapter*")
                    filename(name = "$optionalPackage/junit/CustomJUnit4TestAdapterCache*")
                }
            }
        }
    }
    val needsJunit4 = selector {
        or {
            filename(name = "$optionalPackage/junit/JUnit4TestMethodAdapter*")
            filename(name = "$optionalPackage/junit/CustomJUnit4TestAdapterCache*")
        }
    }
    val needsApacheRegexp = selector {
        filename(name = "$regexpPackage/JakartaRegexp*")
    }
    val needsApacheOro = selector {
        or {
            filename(name = "$regexpPackage/JakartaOro*")
        }
    }
    val needsApacheBcel = selector {
        or {
            filename(name = "$antPackage/filters/util/JavaClassHelper*")
            filename(name = "$utilPackage/depend/bcel/")
            filename(name = "$optionalTypePackage/depend/ClassFileSetTest*")
        }
    }
    val needsApacheLog4j = selector {
        filename(name = "$antPackage/listener/Log4jListener*")
    }
    val needsCommonsLogging = selector {
        filename(name = "$antPackage/listener/CommonsLoggingListener*")
    }
    val needsApacheBsf = selector {
        or {
            filename(name = "$utilPackage/ScriptRunner.*")
            filename(name = "$utilPackage/optional/ScriptRunner*")
        }
    }
    val needsJavamail = selector {
        or {
            filename(name = "$antPackage/taskdefs/email/MimeMailer*")
        }
    }
    val needsNetrexx = selector {
        filename(name = "$optionalPackage/NetRexxC*")
    }
    val needsCommonsNet = selector {
        or {
            filename(name = "$optionalPackage/net/FTP*")
            filename(name = "$optionalPackage/net/RExec*")
            filename(name = "$optionalPackage/net/TelnetTask*")
        }
    }
    val needsAntlr = selector {
        filename(name = "$optionalPackage/ANTLR*")
    }
    val needsJmf = selector {
        filename(name = "$optionalPackage/sound/")
    }
    val needsJai = selector {
        or {
            filename(name = "$optionalPackage/image/")
            filename(name = "$optionalTypePackage/image/")
        }
    }
    val needsJdepend = selector {
        filename(name = "$optionalPackage/jdepend/")
    }
    val needsSwing = selector {
        filename(name = "$optionalPackage/splash/")
    }
    val needsJsch = selector {
        filename(name = "$optionalPackage/ssh/")
    }
    val needsApacheXalan2 = selector {
        filename(name = "$optionalPackage/Xalan2TraceSupport*")
    }
    val antLauncher = selector {
        filename(name = "$antPackage/launch/")
    }
    val antCore = selector {
        not {
            or {
                selector(refid = needsAntlr)
                selector(refid = needsApacheBcel)
                selector(refid = needsApacheBsf)
                selector(refid = needsApacheLog4j)
                selector(refid = needsApacheOro)
                selector(refid = needsApacheRegexp)
                selector(refid = needsApacheResolver)
                selector(refid = needsApacheXalan2)
                selector(refid = needsCommonsLogging)
                selector(refid = needsCommonsNet)
                selector(refid = needsJai)
                selector(refid = needsJavamail)
                selector(refid = needsJdepend)
                selector(refid = needsJmf)
                selector(refid = needsJsch)
                selector(refid = needsJunit)
                selector(refid = needsJunit4)
                selector(refid = needsNetrexx)
                selector(refid = needsSwing)
                selector(refid = antLauncher)
            }
        }
    }
    val onlinetests = patternset {
        exclude(name = "**/GetTest.java", `if` = "offline")
        exclude(name = "**/HttpTest.java", `if` = "offline")
    }
    val teststhatfail = patternset {
        exclude(unless = "run.failing.tests", name = "$optionalPackage/BeanShellScriptTest.java")
        exclude(unless = "run.failing.tests", name = "$optionalPackage/jdepend/JDependTest.java")
    }
    val needsXmlschema = selector {
        or {
            filename(name = "$optionalPackage/SchemaValidateTest.*")
            filename(name = "$optionalPackage/XmlValidateTest.*")
        }
    }
    val usefulTests = patternset {
        include(name = "$antPackage/AntAssert*")
        include(name = "$antPackage/BuildFileTest*")
        include(name = "$antPackage/BuildFileRule*")
        include(name = "$antPackage/FileUtilities*")
        include(name = "$regexpPackage/RegexpMatcherTest*")
        include(name = "$regexpPackage/RegexpTest*")
        include(name = "$optionalPackage/AbstractXSLTLiaisonTest*")
        include(name = "$antPackage/types/AbstractFileSetTest*")
    };
    {
        if (os(family = "mac") && isSet(property = "buildosxpackage.required")) {
            buildosxpackage = true
        }
    }

    val checkForOptionalPackages = target("check_for_optional_packages") {
        if (!equals(arg1 = buildSysclasspath, arg2 = "only")) {
            ignoresystemclasses = true
        }
        ignoresystemclasses = false
        available(property = "jdk1.6+", classname = "java.net.CookieStore")
        available(property = "jdk1.7+", classname = "java.nio.file.FileSystem")
        available(property = "jdk1.8+", classname = "java.lang.reflect.Executable")
        if (contains(string = javaVersion, substring = "1.9.")) {
            jdk19+ = true
        }
        available(property = "kaffe", classname = "kaffe.util.NotImplemented")
        available(property = "harmony", classname = "org.apache.harmony.luni.util.Base64")
        available(property = "bsf.present", classname = "org.apache.bsf.BSFManager", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "netrexx.present", classname = "netrexx.lang.Rexx", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "apache.resolver.present", classname = "org.apache.xml.resolver.tools.CatalogResolver", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "recent.xalan2.present", classname = "org.apache.xalan.trace.TraceListenerEx3", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "junit.present", classname = "junit.framework.TestCase", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "junit4.present", classname = "org.junit.Test", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "antunit.present", classname = "org.apache.ant.antunit.AntUnit", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "commons.net.present", classname = "org.apache.commons.net.ftp.FTPClient", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "antlr.present", classname = "antlr.Tool", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "apache.regexp.present", classname = "org.apache.regexp.RE", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "apache.oro.present", classname = "org.apache.oro.text.regex.Perl5Matcher", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "jmf.present", classname = "javax.sound.sampled.Clip", classpathRef = classpath)
        available(property = "jai.present", classname = "javax.media.jai.JAI", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "jdepend.present", classname = "jdepend.framework.JDepend", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "log4j.present", classname = "org.apache.log4j.Logger", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "commons.logging.present", classname = "org.apache.commons.logging.LogFactory", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "xalan.envcheck", classname = "org.apache.xalan.xslt.EnvironmentCheck", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "which.present", classname = "org.apache.env.Which", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "xerces.present", classname = "org.apache.xerces.parsers.SAXParser", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "bcel.present", classname = "org.apache.bcel.Constants", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        if (available(classname = "javax.activation.DataHandler", classpathRef = classpath) && available(classname = "javax.mail.Transport", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)) {
            javamailComplete = true
        }
        if (equals(arg1 = junitFork.toString(), arg2 = "true") || equals(arg1 = buildSysclasspath, arg2 = "only")) {
            testsAndAntShareClassloader = true
        }
        if (available(classname = "sun.tools.native2ascii.Main") && available(classname = "com.sun.tools.javah.Main")) {
            sunToolsPresent = true
        }
        if (resourcecount(count = 1) {
intersect {
path(path = javaClassPath)
file(file = buildTests)
}
} || isTrue(value = junitFork)) {
            testsAreOnSystemClasspath = true
        }
        echo(level = "verbose") {
            text {
                """
 tests.are.on.system.classpath=$testsAreOnSystemClasspath
                """
            }
        }
        if (available(classname = "org.apache.jasper.compiler.Compiler") && available(classname = "org.apache.jasper.JasperException")) {
            jasperPresent = true
        }
        if (!isSet(property = "kaffe") || available(classname = "javax.swing.ImageIcon", classpathRef = classpath)) {
            swingPresent = true
        }
        if (available(classname = "org.apache.commons.httpclient.HttpClient", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses) && isSet(property = "commons.logging.present")) {
            apacheHttpclientPresent = true
        }
        available(property = "rhino.present", classname = "org.mozilla.javascript.Scriptable", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "beanshell.present", classname = "bsh.StringUtil", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "xerces1.present", classname = "org.apache.xerces.framework.XMLParser", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        available(property = "jsch.present", classname = "com.jcraft.jsch.Session", classpathRef = classpath, ignoresystemclasses = ignoresystemclasses)
        buildCompiler = "modern"
        if (parsersupports(feature = "http://apache.org/xml/features/validation/schema") || parsersupports(feature = "http://java.sun.com/xml/jaxp/properties/schemaSource")) {
            xmlschemaPresent = true
        }
        if (isSet(property = "jdk1.8+") && !isSet(property = "withDoclint")) {
            javadocDoclintNone = "-Xdoclint:none"
        } else {
            javadocDoclintNone = ""
        }
    }

    val prepare = target {
        tstamp {
            format(property = "year", pattern = "yyyy")
        }
        val antFilters = filterchain {
            expandProperties()
        }
    }

    val build = target(::prepare, ::checkForOptionalPackages) {
        mkdir(dir = buildDir)
        mkdir(dir = buildClasses)
        mkdir(dir = buildLib)
        javac(srcdir = javaDir, includeantruntime = false, destdir = buildClasses, debug = debug, deprecation = deprecation, target = javacTarget.toString(), source = javacSource.toString(), optimize = optimize) {
            classpath(refid = classpath)
            val conditionalPatterns = selector {
                not {
                    or {
                        selector(refid = notInKaffe, `if` = "kaffe")
                        selector(refid = needsApacheResolver, unless = "apache.resolver.present")
                        selector(refid = needsJunit, unless = "junit.present")
                        selector(refid = needsJunit4, unless = "junit4.present")
                        selector(refid = needsApacheRegexp, unless = "apache.regexp.present")
                        selector(refid = needsApacheOro, unless = "apache.oro.present")
                        selector(refid = needsApacheBcel, unless = "bcel.present")
                        selector(refid = needsApacheLog4j, unless = "log4j.present")
                        selector(refid = needsCommonsLogging, unless = "commons.logging.present")
                        selector(refid = needsApacheBsf, unless = "bsf.present")
                        selector(refid = needsJavamail, unless = "javamail.complete")
                        selector(refid = needsNetrexx, unless = "netrexx.present")
                        selector(refid = needsCommonsNet, unless = "commons.net.present")
                        selector(refid = needsAntlr, unless = "antlr.present")
                        selector(refid = needsJmf, unless = "jmf.present")
                        selector(refid = needsJai, unless = "jai.present")
                        selector(refid = needsJdepend, unless = "jdepend.present")
                        selector(refid = needsSwing, unless = "swing.present")
                        selector(refid = needsJsch, unless = "jsch.present")
                        selector(refid = needsXmlschema, unless = "xmlschema.present")
                        selector(refid = needsApacheXalan2, unless = "recent.xalan2.present")
                    }
                }
            }
        }
        copy(todir = buildClasses) {
            fileset(dir = javaDir) {
                include(name = "**/*.properties")
                include(name = "**/*.dtd")
                include(name = "**/*.xml")
            }
            fileset(dir = resourceDir)
        }
        copy(todir = buildClasses, overwrite = true, encoding = "UTF-8") {
            fileset(dir = javaDir) {
                include(name = "**/version.txt")
                include(name = "**/defaultManifest.mf")
            }
            filterChain(refid = antFilters)
        }
        copy(todir = "$buildClasses/$optionalPackage/junit/xsl") {
            fileset(dir = etcDir) {
                include(name = "junit-frames.xsl")
                include(name = "junit-noframes.xsl")
            }
        }
    }

    val jars = target(::build) {
        copy(todir = buildDir) {
            fileset(dir = basedir) {
                include(name = "LICENSE")
                include(name = "LICENSE.xerces")
                include(name = "LICENSE.dom")
                include(name = "LICENSE.sax")
                include(name = "NOTICE")
            }
            mapper(`type` = "glob", from = "*", to = "*.txt")
        }
        copy(file = manifest, tofile = manifestTmp)
        manifest(file = manifestTmp) {
            section(name = "$optionalPackage/") {
                attribute(name = "Extension-name", value = "org.apache.tools.ant")
                attribute(name = "Specification-Title", value = "Apache Ant")
                attribute(name = "Specification-Version", value = manifestVersion)
                attribute(name = "Specification-Vendor", value = "Apache Software Foundation")
                attribute(name = "Implementation-Title", value = "org.apache.tools.ant")
                attribute(name = "Implementation-Version", value = manifestVersion)
                attribute(name = "Implementation-Vendor", value = "Apache Software Foundation")
            }
        }
        jar(destFile = "$buildLib/$name-launcher.jar", basedir = buildClasses, whenmanifestonly = "fail") {
            selector(refid = antLauncher)
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
            manifest {
                attribute(name = "Main-Class", value = "org.apache.tools.ant.launch.Launcher")
            }
        }
        jar(destFile = "$buildLib/$name.jar", basedir = buildClasses, manifest = manifest, whenmanifestonly = "fail") {
            selector(refid = antCore)
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
            manifest {
                section(name = "$antPackage/") {
                    attribute(name = "Extension-name", value = "org.apache.tools.ant")
                    attribute(name = "Specification-Title", value = "Apache Ant")
                    attribute(name = "Specification-Version", value = manifestVersion)
                    attribute(name = "Specification-Vendor", value = "Apache Software Foundation")
                    attribute(name = "Implementation-Title", value = "org.apache.tools.ant")
                    attribute(name = "Implementation-Version", value = manifestVersion)
                    attribute(name = "Implementation-Vendor", value = "Apache Software Foundation")
                }
            }
            fileset(dir = manualDir) {
                include(name = "images/ant_logo_large.gif")
            }
        }
        jar(destFile = "$buildLib/$bootstrapJar", basedir = buildClasses, manifest = manifest, whenmanifestonly = "fail") {
            include(name = "$antPackage/Main.class")
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
            manifest {
                attribute(name = "Class-Path", value = "ant.jar xalan.jar")
            }
        }
        optionalJar(dep = "apache-resolver")
        optionalJar(dep = "junit")
        optionalJar(dep = "junit4")
        optionalJar(dep = "apache-regexp")
        optionalJar(dep = "apache-oro")
        optionalJar(dep = "apache-bcel")
        optionalJar(dep = "apache-log4j")
        optionalJar(dep = "commons-logging")
        optionalJar(dep = "apache-bsf")
        optionalJar(dep = "javamail")
        optionalJar(dep = "netrexx")
        optionalJar(dep = "commons-net")
        optionalJar(dep = "antlr")
        optionalJar(dep = "jmf")
        optionalJar(dep = "jai")
        optionalJar(dep = "swing")
        optionalJar(dep = "jsch")
        optionalJar(dep = "jdepend")
        optionalJar(dep = "apache-xalan2")
    }

    val testJar = target("test-jar", ::compileTests) {
        fail(unless = "junit.present") {
            text {
                """

            We cannot build the test jar unless JUnit is present,
            as JUnit is needed to compile the test classes.
                """
            }
        }
        jar(destFile = "$buildLib/$name-testutil.jar", basedir = buildTests) {
            patternSet(refid = usefulTests)
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
        }
    }

    val jarsSources = target("jars-sources") {
        mkdir(dir = buildLibSrc)
        jar(destFile = "$buildLibSrc/$name-launcher.jar", basedir = javaDir, whenmanifestonly = "fail") {
            selector(refid = antLauncher)
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
        }
        jar(destFile = "$buildLibSrc/$name.jar", basedir = javaDir, whenmanifestonly = "fail") {
            selector(refid = antCore)
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
        }
        jar(destFile = "$buildLibSrc/$bootstrapJar", basedir = javaDir, whenmanifestonly = "fail") {
            include(name = "$antPackage/Main.java")
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
        }
        optionalSrcJar(dep = "apache-resolver")
        optionalSrcJar(dep = "junit")
        optionalSrcJar(dep = "junit4")
        optionalSrcJar(dep = "apache-regexp")
        optionalSrcJar(dep = "apache-oro")
        optionalSrcJar(dep = "apache-bcel")
        optionalSrcJar(dep = "apache-log4j")
        optionalSrcJar(dep = "commons-logging")
        optionalSrcJar(dep = "apache-bsf")
        optionalSrcJar(dep = "javamail")
        optionalSrcJar(dep = "netrexx")
        optionalSrcJar(dep = "commons-net")
        optionalSrcJar(dep = "antlr")
        optionalSrcJar(dep = "jmf")
        optionalSrcJar(dep = "jai")
        optionalSrcJar(dep = "swing")
        optionalSrcJar(dep = "jsch")
        optionalSrcJar(dep = "jdepend")
        optionalSrcJar(dep = "apache-xalan2")
    }

    val testJarSource = target("test-jar-source") {
        mkdir(dir = buildLibSrc)
        jar(destFile = "$buildLibSrc/$name-testutil.jar", basedir = javaDir) {
            patternSet(refid = usefulTests)
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
        }
    }

    val distLite = target("dist-lite", ::jars, ::testJar) {
        mkdir(dir = distDir)
        mkdir(dir = distBin)
        mkdir(dir = distLib)
        copy(todir = distLib) {
            fileset(dir = buildLib) {
                exclude(name = bootstrapJar)
            }
        }
        copy(todir = distLib) {
            fileset(dir = libDir) {
                include(name = "*.jar")
                include(name = "*.zip")
            }
        }
        copy(todir = distBin) {
            fileset(dir = scriptDir)
        }
        fixcrlf(srcdir = distBin, eol = "dos", includes = "*.bat,*.cmd")
        fixcrlf(srcdir = distBin, eol = "unix") {
            include(name = "ant")
            include(name = "antRun")
            include(name = "*.pl")
        }
        chmod(perm = "ugo+rx", dir = distDir, `type` = "dir", includes = "**", failonerror = chmodFail)
        chmod(perm = "ugo+r", dir = distDir, `type` = "file", includes = "**", failonerror = chmodFail, maxParallel = chmodMaxparallel)
        chmod(perm = "ugo+x", `type` = "file", failonerror = chmodFail) {
            fileset(dir = distBin) {
                include(name = "**/ant")
                include(name = "**/antRun")
                include(name = "**/*.pl")
                include(name = "**/*.py")
            }
        }
    }

    val dist = target {
        antcall(inheritAll = false, target = "internal_dist") {
            param(name = "dist.dir", value = distName)
        }
    }

    val distJavadocs = target("dist_javadocs", ::javadocs) {
        mkdir(dir = distJavadocs)
        copy(todir = distJavadocs, overwrite = true) {
            fileset(dir = buildJavadocs)
        }
    }

    val internalDist = target("internal_dist", ::distLite, ::distJavadocs) {
        mkdir(dir = distManual)
        mkdir(dir = distEtc)
        copy(todir = distLib, file = "$libDir/README")
        copy(todir = distLib, file = "$libDir/libraries.properties")
        copy(todir = distLib) {
            fileset(dir = "$srcDir/etc/poms") {
                include(name = "*/pom.xml")
            }
            mapper(`type` = "regexp", from = "^(.*)[/\\\\]pom.xml", to = "\\1.pom")
            filterChain {
                tokenFilter {
                    replaceRegex(pattern = pomVersion, replace = projectVersion)
                }
            }
        }
        copy(todir = distLib) {
            fileset(dir = "$srcDir/etc/poms") {
                include(name = "pom.xml")
            }
            mapper(`type` = "glob", from = "pom.xml", to = "ant-parent.pom")
            filterChain {
                tokenFilter {
                    replaceRegex(pattern = pomVersion, replace = projectVersion)
                }
            }
        }
        copy(todir = distManual) {
            fileset(dir = manualDir)
        }
        copy(todir = distDir) {
            fileset(dir = basedir) {
                include(name = "CONTRIBUTORS")
                include(name = "README")
                include(name = "INSTALL")
                include(name = "LICENSE")
                include(name = "LICENSE.xerces")
                include(name = "LICENSE.dom")
                include(name = "LICENSE.sax")
                include(name = "NOTICE")
                include(name = "TODO")
                include(name = "WHATSNEW")
                include(name = "KEYS")
                include(name = "contributors.xml")
                include(name = "fetch.xml")
                include(name = "get-m2.xml")
                include(name = "patch.xml")
            }
        }
        chmod(perm = "ugo+rx", dir = distDir, `type` = "dir", includes = "**", failonerror = chmodFail)
        chmod(perm = "ugo+r", dir = distDir, `type` = "file", includes = "**", failonerror = chmodFail, maxParallel = chmodMaxparallel)
        chmod(perm = "ugo+x", `type` = "file", failonerror = chmodFail) {
            fileset(dir = distBin) {
                include(name = "**/ant")
                include(name = "**/antRun")
                include(name = "**/*.pl")
                include(name = "**/*.py")
            }
        }
        copy(todir = distEtc) {
            fileset(dir = etcDir) {
                include(name = "junit-frames.xsl")
                include(name = "junit-noframes.xsl")
                include(name = "junit-frames-xalan1.xsl")
                include(name = "coverage-frames.xsl")
                include(name = "maudit-frames.xsl")
                include(name = "mmetrics-frames.xsl")
                include(name = "changelog.xsl")
                include(name = "jdepend.xsl")
                include(name = "jdepend-frames.xsl")
                include(name = "checkstyle/*.xsl")
                include(name = "log.xsl")
                include(name = "tagdiff.xsl")
            }
            fileset(dir = buildLib) {
                include(name = bootstrapJar)
            }
        }
    }

    val bootstrap = target {
        antcall(inheritAll = false, target = "dist-lite") {
            param(name = "dist.dir", value = bootstrapDir)
        }
    }

    val srcDist = target("src-dist") {
        mkdir(dir = srcDistDir)
        copy(todir = srcDistLib) {
            fileset(dir = libDir) {
                include(name = "optional/junit*.jar")
                include(name = "README")
                include(name = "libraries.properties")
            }
        }
        mkdir(dir = "$srcDistLib/optional")
        copy(todir = srcDistSrc) {
            fileset(dir = srcDir)
        }
        copy(todir = srcDistManual) {
            fileset(dir = manualDir) {
                exclude(name = "api/")
            }
        }
        copy(todir = srcDistDir) {
            fileset(dir = basedir) {
                include(name = "CONTRIBUTORS")
                include(name = "INSTALL")
                include(name = "KEYS")
                include(name = "LICENSE")
                include(name = "LICENSE.dom")
                include(name = "LICENSE.sax")
                include(name = "LICENSE.xerces")
                include(name = "NOTICE")
                include(name = "README")
                include(name = "TODO")
                include(name = "WHATSNEW")
                include(name = "bootstrap.bat")
                include(name = "bootstrap.sh")
                include(name = "build.bat")
                include(name = "build.sh")
                include(name = "build.xml")
                include(name = "contributors.xml")
                include(name = "fetch.xml")
                include(name = "get-m2.xml")
                include(name = "patch.xml")
            }
        }
        fixcrlf(srcdir = srcDistDir, eol = "dos", includes = "*.bat,*.cmd")
        fixcrlf(srcdir = srcDistDir, eol = "unix") {
            include(name = "**/*.sh")
            include(name = "**/*.pl")
            include(name = "**/ant")
            include(name = "**/antRun")
        }
        fixcrlf(srcdir = srcDistDir) {
            include(name = "**/*.java")
            exclude(name = "$testsEtcDir/taskdefs/fixcrlf/expected/Junk?.java")
            exclude(name = "$testsEtcDir/taskdefs/fixcrlf/input/Junk?.java")
        }
        chmod(perm = "ugo+x", dir = srcDistDir, `type` = "dir", failonerror = chmodFail)
        chmod(perm = "ugo+r", dir = srcDistDir, failonerror = chmodFail)
        chmod(perm = "ugo+x", failonerror = chmodFail) {
            fileset(dir = srcDistDir) {
                include(name = "**/.sh")
                include(name = "**/.pl")
                include(name = "**/.py")
                include(name = "**/ant")
                include(name = "**/antRun")
            }
        }
    }

    val DistributionPrep = target("-distribution_prep") {
        delete(dir = distBase)
        delete(dir = distName)
        delete(dir = javaRepositoryDir)
        mkdir(dir = distBase)
        mkdir(dir = distBaseSource)
        mkdir(dir = distBaseBinaries)
        mkdir(dir = distBaseManual)
        mkdir(dir = javaRepositoryDir)
        antcall(inheritAll = false, target = "internal_dist") {
            param(name = "dist.dir", value = distName)
        }
    }

    val zipDistribution = target("zip_distribution", ::jars, ::DistributionPrep) {
        zip(destFile = "$distBaseBinaries/$distName-bin.zip") {
            zipfileset(dir = "$distName/..", fileMode = "755") {
                include(name = "$distName/bin/ant")
                include(name = "$distName/bin/antRun")
                include(name = "$distName/bin/*.pl")
                include(name = "$distName/bin/*.py")
            }
            fileset(dir = "$distName/..") {
                include(name = "$distName/")
                exclude(name = "$distName/bin/ant")
                exclude(name = "$distName/bin/antRun")
                exclude(name = "$distName/bin/*.pl")
                exclude(name = "$distName/bin/*.py")
            }
        }
    }

    val pkgDistribution = target("pkg_distribution", ::zipDistribution) {
        exec(executable = "release/build-osx-pkg.py") {
            arg(value = "--output-dir")
            arg(value = distBaseBinaries)
            arg(value = "$distBaseBinaries/$distName-bin.zip")
        }
    }

    val tarDistribution = target("tar_distribution", ::jars, ::DistributionPrep) {
        tar(longfile = "gnu", destFile = "$distBaseBinaries/$distName-bin.tar") {
            tarFileSet(dir = "$distName/..", mode = "755", userName = "ant", group = "ant") {
                include(name = "$distName/bin/ant")
                include(name = "$distName/bin/antRun")
                include(name = "$distName/bin/*.pl")
                include(name = "$distName/bin/*.py")
            }
            tarFileSet(dir = "$distName/..", userName = "ant", group = "ant") {
                include(name = "$distName/")
                exclude(name = "$distName/bin/ant")
                exclude(name = "$distName/bin/antRun")
                exclude(name = "$distName/bin/*.pl")
                exclude(name = "$distName/bin/*.py")
            }
        }
        gzip(destfile = "$distBaseBinaries/$distName-bin.tar.gz", src = "$distBaseBinaries/$distName-bin.tar")
        bzip2(destfile = "$distBaseBinaries/$distName-bin.tar.bz2", src = "$distBaseBinaries/$distName-bin.tar")
        delete(file = "$distBaseBinaries/$distName-bin.tar")
    }

    val mainDistribution = target("main_distribution", ::zipDistribution, ::pkgDistribution, ::tarDistribution, ::jarsSources, ::testJarSource) {
        copy(todir = javaRepositoryDir) {
            fileset(dir = "$distName/lib") {
                include(name = "ant*.jar")
            }
            mapper(`type` = "regexp", from = "ant(.*).jar", to = "ant\\1/$projectVersion/ant\\1-$projectVersion.jar")
        }
        copy(todir = javaRepositoryDir) {
            fileset(dir = "$distName/lib") {
                include(name = "*.pom")
            }
            mapper {
                mapper(`type` = "regexp", from = "ant(.*).pom", to = "ant\\1/$projectVersion/ant\\1-$projectVersion.pom")
            }
        }
        copy(todir = javaRepositoryDir) {
            fileset(dir = buildLibSrc) {
                include(name = "ant*.jar")
            }
            mapper(`type` = "regexp", from = "ant(.*).jar", to = "ant\\1/$projectVersion/ant\\1-$projectVersion-sources.jar")
        }
        jar(destFile = "$javaRepositoryDir/ant/$projectVersion/ant-$projectVersion-javadoc.jar", basedir = buildJavadocs) {
            metainf(dir = buildDir) {
                include(name = "LICENSE.txt")
                include(name = "NOTICE.txt")
            }
        }
        checksumsMvn {
            fileset(dir = javaRepositoryDir, includes = "**/*$projectVersion.jar")
            fileset(dir = javaRepositoryDir, includes = "**/*$projectVersion-sources.jar")
            fileset(dir = javaRepositoryDir, includes = "**/*$projectVersion-javadoc.jar")
            fileset(dir = javaRepositoryDir, includes = "**/*$projectVersion.pom")
        }
        zip(destFile = "$distBaseManual/$distName-manual.zip") {
            zipfileset(dir = "$distName/manual", prefix = distName)
            zipfileset(file = "NOTICE", prefix = distName)
        }
        tar(longfile = "gnu", destFile = "$distBaseManual/$distName-manual.tar") {
            tarFileSet(dir = "$distName/manual", prefix = distName)
            tarFileSet(file = "NOTICE", prefix = distName)
        }
        gzip(destfile = "$distBaseManual/$distName-manual.tar.gz", src = "$distBaseManual/$distName-manual.tar")
        bzip2(destfile = "$distBaseManual/$distName-manual.tar.bz2", src = "$distBaseManual/$distName-manual.tar")
        delete(file = "$distBaseManual/$distName-manual.tar")
        delete(dir = distName)
        checksums {
            fileset(dir = "$distBaseBinaries/") {
                exclude(name = "**/*.asc")
                exclude(name = "**/*.md5")
                exclude(name = "**/*.sha1")
                exclude(name = "**/*.sha512")
            }
            fileset(dir = "$distBaseManual/") {
                exclude(name = "**/*.asc")
                exclude(name = "**/*.md5")
                exclude(name = "**/*.sha1")
                exclude(name = "**/*.sha512")
            }
        }
        antcall(inheritAll = false, target = "src-dist") {
            param(name = "src.dist.dir", value = distName)
        }
        zip(destFile = "$distBaseSource/$distName-src.zip") {
            zipfileset(dir = "$distName/..", fileMode = "755") {
                include(name = "$distName/bootstrap.sh")
                include(name = "$distName/build.sh")
            }
            fileset(dir = "$distName/..") {
                include(name = "$distName/")
                exclude(name = "$distName/bootstrap.sh")
                exclude(name = "$distName/build.sh")
            }
        }
        tar(longfile = "gnu", destFile = "$distBaseSource/$distName-src.tar") {
            tarFileSet(dir = "$distName/..", mode = "755", userName = "ant", group = "ant") {
                include(name = "$distName/bootstrap.sh")
                include(name = "$distName/build.sh")
            }
            tarFileSet(dir = "$distName/..", userName = "ant", group = "ant") {
                include(name = "$distName/")
                exclude(name = "$distName/bootstrap.sh")
                exclude(name = "$distName/build.sh")
            }
        }
        gzip(destfile = "$distBaseSource/$distName-src.tar.gz", src = "$distBaseSource/$distName-src.tar")
        bzip2(destfile = "$distBaseSource/$distName-src.tar.bz2", src = "$distBaseSource/$distName-src.tar")
        delete(file = "$distBaseSource/$distName-src.tar")
        delete(dir = distName)
        checksums {
            fileset(dir = "$distBaseSource/") {
                exclude(name = "**/*.asc")
                exclude(name = "**/*.md5")
                exclude(name = "**/*.sha1")
                exclude(name = "**/*.sha512")
            }
        }
    }

    val distribution = target(::mainDistribution) {}

    val clean = target {
        delete(dir = buildDir)
        delete(dir = distBase)
        delete(dir = distDir)
        delete {
            fileset(dir = ".", includes = "**/*~", defaultexcludes = false)
        }
    }

    val allclean = target(::clean) {
        delete(file = "$bootstrapDir/bin/antRun")
        delete(file = "$bootstrapDir/bin/antRun.bat")
        delete(file = "$bootstrapDir/bin/*.pl")
        delete(file = "$bootstrapDir/bin/*.py")
    }

    val install = target {
        fail(message = "You must set the property ant.install=/where/to/install", unless = "ant.install")
        antcall(inheritAll = false, target = "internal_dist") {
            param(name = "dist.dir", value = antInstall)
        }
    }

    val installLite = target("install-lite") {
        fail(message = "You must set the property ant.install=/where/to/install", unless = "ant.install")
        antcall(inheritAll = false, target = "dist-lite") {
            param(name = "dist.dir", value = antInstall)
        }
    }

    val javadocCheck = target("javadoc_check") {
        uptodate(property = "javadoc.notrequired", targetFile = "$buildJavadocs/packages.html") {
            srcfiles(dir = javaDir, includes = "**/*.java")
        }
        uptodate(property = "tests.javadoc.notrequired", targetFile = "$buildTestsJavadocs/packages.html") {
            srcfiles(dir = srcJunit) {
                patternSet(refid = usefulTests)
            }
        }
    }

    val javadocs = target(::prepare, ::javadocCheck, ::checkForOptionalPackages) {
        mkdir(dir = buildJavadocs)
        javadoc(useExternalFile = true, maxmemory = "1000M", destdir = buildJavadocs, author = true, version = true, locale = "en", windowtitle = "$Name API", doctitle = Name, failonerror = true, verbose = javadocVerbose.toBoolean(), additionalparam = javadocDoclintNone) {
            packageset(dir = javaDir)
            tag(name = "todo", description = "To do:", scope = "all")
            tag(name = "ant.task", enabled = false, description = "Task:", scope = "types")
            tag(name = "ant.datatype", enabled = false, description = "Data type:", scope = "types")
            tag(name = "ant.attribute", enabled = false, description = "Attribute:", scope = "types")
            tag(name = "ant.attribute.group", enabled = false, description = "Attribute group:", scope = "types")
            tag(name = "ant.element", enabled = false, description = "Nested element:", scope = "types")
            group(title = "Apache Ant Core", packages = "org.apache.tools.ant*")
            group(title = "Core Tasks", packages = "org.apache.tools.ant.taskdefs*")
            group(title = "Core Types", packages = "org.apache.tools.ant.types*")
            group(title = "Optional Tasks", packages = "org.apache.tools.ant.taskdefs.optional*")
            group(title = "Optional Types", packages = "org.apache.tools.ant.types.optional*")
            group(title = "Ant Utilities", packages = "org.apache.tools.ant.util*")
            classpath(refid = testsClasspath)
        }
    }

    val testJavadocs = target("test-javadocs", ::prepare, ::javadocCheck) {
        mkdir(dir = buildTestsJavadocs)
        javadoc(useExternalFile = true, destdir = buildTestsJavadocs, failonerror = true, author = true, version = true, locale = "en", windowtitle = "$Name Test Utilities", doctitle = Name, additionalparam = javadocDoclintNone) {
            tag(name = "pre", description = "Precondition:", scope = "all")
            fileset(dir = srcJunit) {
                patternSet(refid = usefulTests)
            }
            classpath(refid = testsClasspath)
        }
    }

    val compileTests = target("compile-tests", ::build) {
        mkdir(dir = buildTests)
        javac(srcdir = srcJunit, includeantruntime = false, destdir = buildTests, debug = debug, target = javacTarget.toString(), source = javacSource.toString(), deprecation = deprecation) {
            classpath(refid = testsClasspath)
            selector(refid = conditionalPatterns)
        }
        jar(jarfile = "$buildTests/org/apache/tools/ant/taskdefs/test2-antlib.jar") {
            manifest {
                attribute(name = "Extension-name", value = "org.apache.tools.ant")
                attribute(name = "Specification-Title", value = "Apache Ant")
                attribute(name = "Specification-Version", value = manifestVersion)
                attribute(name = "Specification-Vendor", value = "Apache Software Foundation")
                attribute(name = "Implementation-Title", value = "org.apache.tools.ant")
                attribute(name = "Implementation-Version", value = manifestVersion)
                attribute(name = "Implementation-Vendor", value = "Apache Software Foundation")
            }
            zipfileset(dir = testsEtcDir, fullpath = "taskdefs/test.antlib.xml") {
                include(name = "taskdefs/test2.antlib.xml")
            }
        }
    }

    val dumpInfo = target("dump-info", ::dumpSysProperties, ::runWhich) {}

    val dumpSysProperties = target("dump-sys-properties", ::xmlCheck) {
        echo(message = "java.vm.info=$javaVmInfo")
        echo(message = "java.vm.name=$javaVmName")
        echo(message = "java.vm.vendor=$javaVmVendor")
        echo(message = "java.vm.version=$javaVmVersion")
        echo(message = "os.arch=$osArch")
        echo(message = "os.name=$osName")
        echo(message = "os.version=$osVersion")
        echo(message = "file.encoding=$fileEncoding")
        echo(message = "user.language=$userLanguage")
        echo(message = "ant.version=$antVersion")
    }

    val xmlCheck = target("xml-check", ::checkForOptionalPackages) {
        java(classname = "org.apache.xalan.xslt.EnvironmentCheck")
    }

    val runWhich = target("run-which", ::checkForOptionalPackages) {
        java(classname = "org.apache.env.Which", taskname = "which", classpathRef = classpath)
    }

    val probeOffline = target("probe-offline") {
        if (isSet(property = "offline") || !http(url = "http://www.apache.org/")) {
            offline = true
        }
        echo(level = "verbose") {
            text {
                """
 offline=$offline
                """
            }
        }
    }

    val checkFailed = target("check-failed") {
        if (isSet(property = "junit.failed") || isSet(property = "antunit.failed")) {
            testsFailed = true
        }
    }

    val test = target(::dumpInfo, ::junitReport, ::antunitReport, ::checkFailed) {
        fail(`if` = "tests.failed", unless = "ignore.tests.failed") {
            text {
                """
Unit tests failed; see:
            $buildJunitReports
            $antunitReports
        
                """
            }
        }
    }

    val runTests = target("run-tests", ::dumpInfo, ::junitTests, ::antunitTests, ::checkFailed) {
        fail(`if` = "tests.failed", message = "Unit tests failed")
    }

    val testInit = target("test-init", ::probeOffline, ::checkForOptionalPackages) {
        mkdir(dir = buildJunitTmpdir)
        fail {
            text {
                """
"testcase" cannot be specified with "junit.testcase" or "antunit.testcase".
            
                """
            }
            condition {
                and {
                    isSet(property = "testcase")
                    or {
                        isSet(property = "antunit.testcase")
                        isSet(property = "junit.testcase")
                    }
                }
            }
        }
        if (available(file = "$srcAntunit/$testcase")) {
            antunitTestcase = testcase
        }
        if (available(classname = testcase, classpathRef = testsRuntimeClasspath, ignoresystemclasses = ignoresystemclasses)) {
            junitTestcase = testcase
        }
        fail {
            text {
                """
Cannot locate test $testcase
            
                """
            }
            condition {
                and {
                    isSet(property = "testcase")
                    not {
                        or {
                            isSet(property = "antunit.testcase")
                            isSet(property = "junit.testcase")
                        }
                    }
                }
            }
        }
        if (!equals(arg1 = testcase, arg2 = antunitTestcase) && isSet(property = "junit.present")) {
            runJunit = true
        }
        if (isSet(property = "junit.testcase") && isSet(property = "run.junit")) {
            junitSingle = true
        }
        if (!isSet(property = "junit.testcase") && isSet(property = "run.junit")) {
            junitBatch = true
        }
        if (!equals(arg1 = testcase, arg2 = junitTestcase) && isSet(property = "antunit.present")) {
            runAntunit = true
        }
        if (isset(property = "run.antunit")) {
            runAntunitReport = true
        }
        if (isset(property = "run.junit")) {
            runJunitReport = true
        }
    }

    val junitReport = target("junit-report", ::junitTests, ::junitReportOnly) {}

    val junitReportOnly = target("junit-report-only", ::testInit) {
        mkdir(dir = buildJunitReports)
        junitreport(todir = buildJunitReports) {
            fileset(dir = buildJunitXml) {
                include(name = "TEST-*.xml")
            }
            report(format = "frames", todir = buildJunitReports)
        }
    }

    val junitTests = target("junit-tests", ::junitBatch, ::junitSingleTest) {}

    val junitBatch = target("junit-batch", ::compileTests, ::testInit) {
        junitIncludes = "**/*Test*"
        junitExcludes = ""
        testJunit {
            formatter(`type` = "brief", usefile = "false")
            batchtest(todir = buildJunitXml, unless = "hasFailingTests") {
                fileset(dir = srcJunit, includes = junitIncludes, excludes = junitExcludes) {
                    exclude(name = "$taskdefsPackage/TaskdefsTest.java")
                    exclude(name = "$antPackage/BuildFileTest.java")
                    exclude(name = "$regexpPackage/RegexpMatcherTest.java")
                    exclude(name = "$regexpPackage/RegexpTest.java")
                    exclude(name = "$optionalPackage/AbstractXSLTLiaisonTest.java")
                    exclude(name = "$antPackage/types/AbstractFileSetTest.java")
                    exclude(name = "$antPackage/types/selectors/BaseSelectorTest.java")
                    exclude(name = "org/example/")
                    exclude(name = "$taskdefsPackage/TaskdefTest*Task.java")
                    exclude(name = "$optionalPackage/junit/TestFormatter.java")
                    exclude(name = "$taskdefsPackage/TestProcess.java")
                    exclude(name = "$optionalPackage/splash/SplashScreenTest.java")
                    selector(refid = conditionalPatterns)
                    patternSet(refid = onlinetests)
                    patternSet(refid = teststhatfail)
                    exclude(name = "$optionalPackage/Rhino*.java", unless = "bsf.present")
                    exclude(name = "$optionalPackage/Rhino*.java", unless = "rhino.present")
                    exclude(name = "$optionalPackage/script/*.java", unless = "bsf.present")
                    exclude(name = "$optionalPackage/script/*.java", unless = "rhino.present")
                    exclude(name = "$optionalPackage/BeanShellScriptTest.java", unless = "bsf.present")
                    exclude(name = "$optionalPackage/BeanShellScriptTest.java", unless = "beanshell.present")
                    exclude(name = "$optionalTypePackage/Script*.java", unless = "bsf.present")
                    exclude(name = "$optionalTypePackage/Script*.java", unless = "rhino.present")
                    exclude(name = "$antPackage/AntClassLoaderDelegationTest.java", `if` = "tests.are.on.system.classpath")
                    exclude(name = "$optionalPackage/junit/JUnitClassLoaderTest.java", `if` = "tests.are.on.system.classpath")
                    exclude(name = "$optionalPackage/PvcsTest.java")
                    exclude(name = "$optionalPackage/junit/JUnitReportTest.java", unless = "run.junitreport")
                    exclude(name = "$antPackage/IncludeTest.java", unless = "xerces1.present")
                    exclude(name = "$typePackage/selectors/ModifiedSelectorTest.java", unless = "xerces1.present")
                    exclude(name = "$optionalPackage/XmlValidateCatalogTest.java", unless = "apache.resolver.present")
                    exclude(name = "$optionalPackage/JspcTest.java", unless = "jasper.present")
                    exclude(name = "$taskdefsPackage/SQLExecTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$taskdefsPackage/cvslib/ChangeLogWriterTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$taskdefsPackage/cvslib/ChangeLogParserTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$optionalPackage/sos/SOSTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$optionalPackage/vss/MSVSSTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$optionalPackage/TraXLiaisonTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$taskdefsPackage/ProcessDestroyerTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$taskdefsPackage/ProtectedJarMethodsTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$antPackage/launch/LocatorTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$antPackage/DefaultLoggerTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$taskdefsPackage/ZipExtraFieldTest.java", unless = "tests.and.ant.share.classloader")
                    exclude(name = "$taskdefsPackage/AbstractCvsTaskTest.java", unless = "have.cvs")
                    exclude(name = "$optionalPackage/net/FTPTest.java")
                    exclude(name = "$optionalPackage/ssh/ScpTest.java")
                    exclude(name = "$antPackage/util/ClasspathUtilsTest.java", `if` = "tests.and.ant.share.classloader")
                }
            }
        }
    }

    val junitSingleTest = target("junit-single-test", ::compileTests, ::junitSingleTestOnly) {}

    val junitSingleTestOnly = target("junit-single-test-only", ::testInit) {
        testJunit {
            formatter(`type` = "plain", usefile = "false")
            test(name = junitTestcase, todir = buildJunitXml)
        }
    }

    val interactiveTests = target("interactive-tests", ::compileTests) {
        java(classpathRef = testsRuntimeClasspath, classname = "org.apache.tools.ant.taskdefs.TestProcess", fork = true)
    }

    val AntunitCheckLocation = target("-antunit-check-location") {
        if (equals(arg1 = antHome, arg2 = bootstrapDir) || equals(arg1 = antHome, arg2 = distDir)) {
            antunitRecommendedLocation = true
        }
    }

    val AntunitWarnLocation = target("-antunit-warn-location", ::AntunitCheckLocation) {
        echo {
            text {
                """
AntUnit tests must be run with $bootstrapDir (or $distDir), not $antHome. Try './build.sh antunit-tests' for example.
                """
            }
        }
    }

    val antunitTests = target("antunit-tests", ::dumpInfo, ::build, ::testInit, ::AntunitWarnLocation) {
        if (isset(property = "antunit.testcase")) {
            antunitIncludes = antunitTestcase
        } else {
            antunitIncludes = "**/test.xml,**/*-test.xml"
        }
        antunitExcludes = ""
        mkdir(dir = antunitXml)
        au:antunit(xmlns:au = "antlib:org.apache.ant.antunit", failonerror = "false", errorproperty = "antunit.failed") {
            fileset(dir = srcAntunit, includes = antunitIncludes, excludes = antunitExcludes)
            au:plainlistener(logLevel = antunitLoglevel)
            au:xmllistener(todir = antunitXml)
            propertyset {
                propertyref(name = "antunit.tmpdir")
                propertyref(name = "ant.home")
            }
        }
    }

    val antunitReport = target("antunit-report", ::antunitTests, ::antunitReportOnly) {}

    val antunitReportOnly = target("antunit-report-only", ::testInit) {
        length {
            fileset(dir = antunitXml, includes = "TEST-*.xml")
        }
        mkdir(dir = antunitReports)
        junitreport(todir = antunitReports) {
            fileset(dir = antunitXml, includes = "TEST-*.xml")
            report(styledir = srcAntunit, format = "frames", todir = antunitReports)
        }
        length {
            fileset(dir = antunitXml, includes = "TEST-*.xml")
        }
    }

    val printFailingTests = target {
        failingtestsDir = "$buildDir/errors"
        mkdir(dir = "")
        xslt(style = "$etcDir/printFailingTests.xsl", destdir = failingtestsDir, extension = ".txt", basedir = buildDir, includes = "testcases/**/TEST-*.xml,antunit/xml/TEST-*.xml")
        echo {
            text {
                """
+-------------------------------------------------------------------------------------
                """
            }
        }
        echo {
            text {
                """
| FAILING TESTS:
                """
            }
        }
        echo {
            text {
                """
+-------------------------------------------------------------------------------------
                """
            }
        }
        concat {
            fileset(dir = failingtestsDir) {
                size(value = 0, `when` = "more")
            }
            filterChain {
                lineContains {
                    contains(value = "|")
                }
            }
        }
        echo {
            text {
                """
+-------------------------------------------------------------------------------------
                """
            }
        }
    }

    [default]
    val main = target(::distLite) {}

    val msi = target(::internalDist) {
        msiDir = buildDir
        msiName = "$name-$projectVersion.msi"
        msiFile = "$msiDir/$msiName"
        wixHome = "$userHome/wix"
        wixobjDir = "$buildDir/wix"
        property(name = "dist.dir.resolved", location = distDir)
        mkdir(dir = wixobjDir)
        dn:wix(target = msiFile, mode = "both", wixHome = wixHome, wixobjDestDir = wixobjDir) {
            sources(dir = etcDir, includes = "*.wxs")
            moresources(dir = distDir)
            candleParameter(name = "dist.dir", value = distDirResolved)
            candleParameter(name = "version", value = manifestVersion)
        }
    }
}
