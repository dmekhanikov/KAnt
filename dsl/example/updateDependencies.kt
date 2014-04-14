import ru.ifmo.rain.mekhanikov.antdsl.*

val osTag: String by StringProperty("tar.gz")
val jbBuildserverBuildId: String by StringProperty("2462412")
val publicBuildserverBuildId: String by StringProperty("128334")
val ideaBuildNumber: String by StringProperty("135.SNAPSHOT")
val ideaArchiveName: String by StringProperty("ideaIC-" + ideaBuildNumber + "." + osTag)
val ideaSdkFetchNeeded: Boolean by BooleanProperty(true)
val continuousIdeaVersion: String by StringProperty(".lastSuccessful")

val androidOsTag: String by StringProperty("linux")
val androidBuildVersion: String by StringProperty("")
val androidVersion: String by StringProperty("")

val androidFileName: String by StringProperty("android-studio-ide-" + androidBuildVersion + "-" + androidOsTag + ".zip")
val androidStudioUrl: String by StringProperty("http://dl.google.com/dl/android/studio/ide-zips/" + androidVersion + "/" + androidFileName)
val androidDestinationDir: String by StringProperty("android-studio/sdk")
val core: String by StringProperty("ideaSDK/core")
val jps: String by StringProperty("ideaSDK/jps")
val jpsTest: String by StringProperty(jps + "/test")

fun DSLTaskContainer.getMavenLibrary(prefix: String, lib: String, version: String, bin: Boolean = true, src: Boolean = true, server: String = "http://repository.jetbrains.com/remote-repos", jarNameBase: String = lib + "-" + version, targetJarNameBase: String = jarNameBase, path: String = prefix + "/" + lib + "/" + version + "/" + jarNameBase, download: String = "dependencies/download", dependencies: String = "dependencies") {
    sequential {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties", classpath = basedir + "/dependencies/ant-contrib.jar")
        if (bin) {
            get(src = server + "/" + path + ".jar", dest = download + "/" + jarNameBase + ".jar", usetimestamp = true)
            copy(file = download + "/" + jarNameBase + ".jar", tofile = dependencies + "/" + targetJarNameBase + ".jar", overwrite = true)
        }
        if (src){
            get(src = server + "/" + path + "-sources.jar", dest = download + "/" + jarNameBase + "-sources.jar", usetimestamp = true)
            copy(file = download + "/" + jarNameBase + "-sources.jar", tofile = dependencies + "/" + targetJarNameBase + "-sources.jar", overwrite = true)
        }
    }
}

fun DSLTaskContainer.getAntLibrary(version: String, foldername: String) {
    sequential {
        get(src = "http://archive.apache.org/dist/ant/binaries/apache-ant-" + version + "-bin.tar.gz", dest = "dependencies/download/apache-ant-" + version + "-bin.tar.gz", usetimestamp = true)
        get(src = "http://archive.apache.org/dist/ant/source/apache-ant-" + version + "-src.zip", dest = "dependencies/apache-ant-" + version + "-src.zip", usetimestamp = true)
        delete(dir = "dependencies/" + foldername, failonerror = false)
        untar(src = "dependencies/download/apache-ant-" + version + "-bin.tar.gz", dest = "dependencies", compression = "gzip")
        move(file = "dependencies/apache-ant-" + version, tofile = "dependencies/" + foldername)
    }
}

fun DSLTaskContainer.getAsmSourcesAndRenamePackages(asmVersion: String) {
    sequential {
        getMavenLibrary(prefix = "org/ow2/asm", lib = "asm-debug-all", version = asmVersion, bin = false)
        delete(dir = "dependencies/download/asm-src", failonerror = false)
        expand(src = "dependencies/download/asm-debug-all-" + asmVersion + "-sources.jar", dest = "dependencies/download/asm-src") {
            patternset {
                include(name = "**/*")
            }
        }
        replaceregexp(match = "org\.objectweb\.asm", replace = "org.jetbrains.org.objectweb.asm", flags = "g") {
            fileset(dir = "dependencies/download/asm-src/") {
                include(name = "**/*.java")
            }
        }
        move(file = "dependencies/download/asm-src/org/objectweb/asm", tofile = "dependencies/download/asm-src/org/jetbrains/org/objectweb/asm")
        zip(destfile = "dependencies/jetbrains-asm-all-" + asmVersion + "-src.zip", basedir = "dependencies/download/asm-src")
    }
}

fun DSLTaskContainer.executeUpdate(baseUrl: String = "http://teamcity.example.com/guestAuth/repository/download/btXXX/XXXX:id", baseUrlForCore: String = baseUrl, buildZip: String = "ideaIC-XXX.SNAPSHOT.win.zip") {
    sequential {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties", classpath = basedir + "/dependencies/ant-contrib.jar")
        if (ideaSdkFetchNeeded) {
            delete(dir = "ideaSDK", failonerror = false) {
                exclude(name = "config/**")
                exclude(name = "system/**")
                exclude(name = "system-idea/**")
            }
            mkdir(dir = core)
            mkdir(dir = jps)
            mkdir(dir = jpsTest)
            get(src = baseUrlForCore + "/core/intellij-core.jar", dest = core + "/intellij-core.jar", usetimestamp = true)
            get(src = baseUrl + "/core/annotations.jar", dest = core + "/annotations.jar", usetimestamp = true)
            get(src = baseUrl + "/core/guava-14.0.1.jar", dest = core + "/guava-14.0.1.jar", usetimestamp = true)
            get(src = baseUrl + "/core/picocontainer.jar", dest = core + "/picocontainer.jar", usetimestamp = true)
            get(src = baseUrl + "/core/trove4j.jar", dest = core + "/trove4j.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/groovy-jps-plugin.jar", dest = jps + "/groovy-jps-plugin.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/groovy_rt.jar", dest = jps + "/groovy_rt.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/jdom.jar", dest = jps + "/jdom.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/jgoodies-forms.jar", dest = jps + "/jgoodies-forms.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/jna.jar", dest = jps + "/jna.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/jps-builders.jar", dest = jps + "/jps-builders.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/jps-model.jar", dest = jps + "/jps-model.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/log4j.jar", dest = jps + "/log4j.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/nanoxml-2.2.3.jar", dest = jps + "/nanoxml-2.2.3.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/protobuf-2.5.0.jar", dest = jps + "/protobuf-2.5.0.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/trove4j.jar", dest = jps + "/trove4j.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/ui-designer-jps-plugin.jar", dest = jps + "/ui-designer-jps-plugin.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/util.jar", dest = jps + "/util.jar", usetimestamp = true)
            get(src = baseUrl + "/jps/test/jps-build-test.jar", dest = jpsTest + "/jps-build-test.jar", usetimestamp = true)
            get(src = baseUrl + "/" + buildZip, dest = "dependencies/download/" + buildZip, usetimestamp = true)
            delete(file = "dependencies/download/idea-sdk-sources.zip", failonerror = false)
            get(src = baseUrl + "/sources.zip", dest = "dependencies/download/idea-sdk-sources.zip", usetimestamp = true)
        }
        if (matches(pattern = ".+\.win\.zip", string = buildZip)) {
            expand(src = "dependencies/download/" + buildZip, dest = "ideaSDK")
        } else if (matches(pattern = ".+\.mac\.zip", string = buildZip)) {
            expand(src = "dependencies/download/" + buildZip, dest = "ideaSDK") {
                cutdirsmapper(dirs = "1")
            }
            exectask(executable = "chmod") {
                arg(value = "a+x")
                arg(path = "ideaSDK/bin/fsnotifier")
                arg(path = "ideaSDK/bin/inspect.sh")
                arg(path = "ideaSDK/bin/printenv.py")
                arg(path = "ideaSDK/bin/restarter")
            }
        } else {
            untar(src = "dependencies/download/" + buildZip, dest = "ideaSDK", compression = "gzip") {
                cutdirsmapper(dirs = "1")
            }
            exectask(executable = "chmod") {
                arg(value = "a+x")
                arg(path = "ideaSDK/bin/fsnotifier")
                arg(path = "ideaSDK/bin/fsnotifier64")
                arg(path = "ideaSDK/bin/inspect.sh")
                arg(path = "ideaSDK/bin/idea.sh")
            }
        }
        mkdir(dir = "ideaSDK/sources")
        copy(file = "dependencies/download/idea-sdk-sources.zip", tofile = "ideaSDK/sources/sources.zip", overwrite = true)
        copy(file = "ideaSDK/lib/jdom.jar", todir = core)
        copy(file = "ideaSDK/lib/jna.jar", todir = core)
        copy(file = "ideaSDK/lib/log4j.jar", todir = core)
        copy(file = "ideaSDK/lib/xstream-1.4.3.jar", todir = core)
        copy(file = "ideaSDK/lib/xpp3-1.1.4-min.jar", todir = core)
        copy(file = "ideaSDK/lib/jsr166e.jar", todir = core)
        copy(file = "ideaSDK/lib/asm-all.jar", todir = core)
        copy(file = "ideaSDK/lib/util.jar", todir = core)
        delete(file = "ideaSDK/lib/junit.jar")
    }
}

fun main(args: Array<String>) {
    project(args) {
        default = target(name = "update", depends = "fetch-third-party,fetch-annotations") {
            executeUpdate(baseUrl = "http://teamcity.jetbrains.com/guestAuth/repository/download/bt410/" + publicBuildserverBuildId + ":id", buildZip = ideaArchiveName)
        }
        target(name = "jb_update", depends = "fetch-third-party,fetch-annotations") {
            executeUpdate(baseUrl = "http://buildserver.labs.intellij.net/guestAuth/repository/download/bt3498/" + jbBuildserverBuildId + ":id", buildZip = ideaArchiveName)
        }
        target(name = "jb_update_continuous_local", depends = "fetch-third-party,fetch-annotations") {
            executeUpdate(baseUrl = "http://buildserver.labs.intellij.net/guestAuth/repository/download/ijplatform_IjPlatform13_IdeaTrunk_Installers/" + continuousIdeaVersion, buildZip = "ideaIC-{build.number}.win.zip")
        }
        target(name = "jb_update_continuous", depends = "fetch-third-party,fetch-annotations") {
            executeUpdate(baseUrl = "file:///" + basedir + "/idea_artifacts", buildZip = ideaArchiveName)
        }

        target(name = "fetch-third-party") {
            mkdir(dir = "dependencies")
            mkdir(dir = "dependencies/download")
            get(src = "http://heanet.dl.sourceforge.net/project/proguard/proguard%20beta/4.8beta/proguard4.8beta1.zip", dest = "dependencies/download/proguard4.8beta1.zip", usetimestamp = true)
            delete(file = "dependencies/proguard.jar", failonerror = false)
            expand(src = "dependencies/download/proguard4.8beta1.zip", dest = "dependencies") {
                patternset {
                    include(name = "proguard4.8beta1/lib/proguard.jar")
                }
                mapper(`type` = "flatten")
            }
            get(src = "http://heanet.dl.sourceforge.net/project/ant-contrib/ant-contrib/1.0b3/ant-contrib-1.0b3-bin.zip", dest = "dependencies/download/ant-contrib-1.0b3-bin.zip", usetimestamp = true)
            delete(file = "dependencies/ant-contrib.jar", failonerror = false)
            expand(src = "dependencies/download/ant-contrib-1.0b3-bin.zip", dest = "dependencies") {
                patternset {
                    include(name = "ant-contrib/ant-contrib-1.0b3.jar")
                }
                mapper(`type` = "merge", to = "ant-contrib.jar")
            }
            get(src = "http://jarjar.googlecode.com/files/jarjar-1.2.jar", dest = "dependencies/download/jarjar-1.2.jar", usetimestamp = true)
            copy(file = "dependencies/download/jarjar-1.2.jar", tofile = "dependencies/jarjar.jar", overwrite = true)
            getAntLibrary(version = "1.7.0", foldername = "ant-1.7")
            getAntLibrary(version = "1.8.0", foldername = "ant-1.8")
            getMavenLibrary(prefix = "com/google/android/tools", lib = "dx", version = "1.7", targetJarNameBase = "dx")
            getMavenLibrary(prefix = "org/hamcrest", lib = "hamcrest-core", version = "1.3", targetJarNameBase = "hamcrest-core")
            mkdir(dir = "dependencies/jflex")
            get(src = "https://raw.github.com/JetBrains/intellij-community/master/tools/lexer/jflex-1.4/lib/JFlex.jar", dest = "dependencies/jflex/JFlex.jar", usetimestamp = true)
            get(src = "https://raw.github.com/JetBrains/intellij-community/master/tools/lexer/idea-flex.skeleton", dest = "dependencies/jflex/idea-flex.skeleton", usetimestamp = true)
            getMavenLibrary(prefix = "jline", lib = "jline", version = "2.9", targetJarNameBase = "jline")
            getMavenLibrary(prefix = "com/google/guava", lib = "guava", version = "14.0.1", bin = false)
            get(src = "https://raw.github.com/JetBrains/intellij-community/master/lib/src/asm5-src.zip", dest = "dependencies/asm5-src.zip")
            getMavenLibrary(prefix = "com/google/protobuf", lib = "protobuf-java", version = "2.5.0", bin = false)
            getMavenLibrary(prefix = "com/github/spullara/cli-parser", lib = "cli-parser", version = "1.1.1")
            get(src = "http://dl.google.com/closure-compiler/compiler-20131014.zip", dest = "dependencies/download/closure-compiler.zip", usetimestamp = true)
            delete(file = "dependencies/closure-compiler.jar", failonerror = false)
            expand(src = "dependencies/download/closure-compiler.zip", dest = "dependencies") {
                patternset {
                    include(name = "compiler.jar")
                }
                mapper(`type` = "merge", to = "closure-compiler.jar")
            }
            delete(file = "dependencies/android.jar", failonerror = false)
            get(src = "http://dl-ssl.google.com/android/repository/android-19_r02.zip", dest = "dependencies/download/android-sdk.zip", usetimestamp = true)
            expand(src = "dependencies/download/android-sdk.zip", dest = "dependencies") {
                patternset {
                    include(name = "**/android.jar")
                }
                mapper(`type` = "flatten")
            }
            get(src = "http://teamcity.jetbrains.com/guestAuth/repository/download/bt345/bootstrap.tcbuildtag/kotlin-plugin-{build.number}.zip", dest = "dependencies/download/bootstrap-compiler.zip", usetimestamp = true)
            delete(dir = "dependencies/bootstrap-compiler", failonerror = false)
            expand(src = "dependencies/download/bootstrap-compiler.zip", dest = "dependencies/bootstrap-compiler")
        }

        target(name = "fetch-annotations") {
            mkdir(dir = "dependencies/annotations")
            get(src = "http://teamcity.jetbrains.com/guestAuth/repository/download/Kotlin_KAnnotator_InferJdkAnnotations/shipWithKotlin.tcbuildtag/kotlin-jdk-annotations.jar", dest = "dependencies/annotations/kotlin-jdk-annotations.jar", usetimestamp = true)
            get(src = "http://teamcity.jetbrains.com/guestAuth/repository/download/Kotlin_KAnnotator_InferJdkAnnotations/shipWithKotlin.tcbuildtag/kotlin-android-sdk-annotations.jar", dest = "dependencies/annotations/kotlin-android-sdk-annotations.jar", usetimestamp = true)
        }
        target(name = "get_android_studio") {
            taskdef(resource = "net/sf/antcontrib/antcontrib.properties", classpath = "$basedir/dependencies/ant-contrib.jar")
            if (!(isset(property = "android.version") && isset(property = "android.build.version"))) {
                loadresource(property = "android.version") {
                    url(url = "http://tools.android.com/download/studio/canary/latest")
                    filterchain {
                        tokenfilter {
                            filetokenizer()
                            replaceregex(pattern = "^(.*)http://dl\.google\.com/dl/android/studio/ide-zips/([\d\.]+)/android-studio-ide(.*)$", replace = "\2", flags = "s")
                        }
                    }
                }
                loadresource(property = "android.build.version") {
                    url(url = "http://tools.android.com/download/studio/canary/latest")
                    filterchain {
                        tokenfilter {
                            filetokenizer()
                            replaceregex(pattern = "^(.*)http://dl\.google\.com/dl/android/studio/ide-zips/[\d\.]+/android-studio-ide-([\d\.]+)-(.*)$", replace = "\2", flags = "s")
                        }
                    }
                }
            }
            echo(message = "Download android studio: $androidVersion $androidBuildVersion")

            mkdir(dir = "dependencies/download")
            get(src = androidStudioUrl, dest = "dependencies/download", usetimestamp = true)
            delete(dir = androidDestinationDir, failonerror = false, includeemptydirs = true) {
                exclude(name = "config/**")
                exclude(name = "system/**")
            }
            expand(src = "dependencies/download/" + androidFileName, dest = androidDestinationDir) {
                cutdirsmapper(dirs = "1")
            }
            if (matches(pattern = ".+windows\.zip", string = androidFileName)){
            } else if (matches(pattern = ".+mac\.zip", string = androidFileName)) {
                exectask(executable = "chmod") {
                    arg(value = "a+x")
                    arg(path = androidDestinationDir + "/bin/fsnotifier")
                    arg(path = androidDestinationDir + "/bin/inspect.sh")
                    arg(path = androidDestinationDir + "/bin/printenv.py")
                    arg(path = androidDestinationDir + "/bin/update_studio.sh")
                }
            } else if (matches(pattern = ".+linux\.zip", string = androidFileName)) {
                exectask(executable = "chmod") {
                    arg(value = "a+x")
                    arg(path = androidDestinationDir + "/bin/fsnotifier")
                    arg(path = androidDestinationDir + "/bin/fsnotifier64")
                    arg(path = androidDestinationDir + "/bin/inspect.sh")
                    arg(path = androidDestinationDir + "/bin/studio.sh")
                    arg(path = androidDestinationDir + "/bin/update_studio.sh")
                }
            } else {
                exit(message = "File name '" + androidFileName + "' wasn't matched")
            }
        }
    }
}