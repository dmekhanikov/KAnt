package jetbrains.kant.dsl.ivy

import java.io.File
import java.util.ArrayList
import java.io.FileWriter
import org.apache.ivy.Ivy
import org.apache.ivy.core.retrieve.RetrieveOptions
import org.apache.ivy.core.module.descriptor.ModuleDescriptor

class XmlTag(val name: String) {
    private val TAB = "    "
    private val attributes = ArrayList<Pair<String, String>>()
    private val children = ArrayList<XmlTag>()
    private var indent = ""
    var parent: XmlTag? = null
    var revId = Integer.MIN_VALUE

    public fun addAttribute(key: String, value: Any) {
        attributes.add(Pair(key, value.toString()))
    }

    public fun maybeAddAttribute(key: String, value: Any?) {
        if (value != null) {
            addAttribute(key, value)
        }
    }

    public fun addChild(child: XmlTag) {
        children.add(child)
        child.indent = indent + TAB
        child.parent = this
        incRevId()
    }

    override public  fun toString(): String {
        val sb = StringBuilder("$indent<$name")
        for (attribute in attributes) {
            sb.append(" ${attribute.first}=\"${attribute.second}\"")
        }
        if (children.isEmpty()) {
            sb.append(" />")
        } else {
            sb.append(">")
            for (child in children) {
                sb.append('\n' + child.toString())
            }
            sb.append("\n$indent</$name>")
        }
        return sb.toString()
    }

    fun incRevId() {
        revId++
        parent?.incRevId()
    }
}

public class IvyModuleDescriptor {
    private val ivyModuleTag = XmlTag("ivy-module")
    private val ivyFile = File.createTempFile("ivy", ".xml")
    private var lastIvyModuleRevId = ivyModuleTag.revId
    {
        ivyModuleTag.addAttribute("version", "2.0")
        ivyFile.deleteOnExit()
    }

    public fun info(organisation: String,
                    module: String,
                    branch: String? = null,
                    revision: String? = null,
                    status: String? = null,
                    publication: String? = null) {
        val infoTag = XmlTag("info")
        ivyModuleTag.addChild(infoTag)
        infoTag.addAttribute("organisation", organisation)
        infoTag.addAttribute("module", module)
        infoTag.maybeAddAttribute("branch", branch)
        infoTag.maybeAddAttribute("revision", revision)
        infoTag.maybeAddAttribute("status", status)
        infoTag.maybeAddAttribute("publication", publication)
    }

    public fun dependencies(defaultConf: String? = null,
                            defaultConfMapping: String? = null,
                            init: IvyDependencies.() -> Unit) {
        val dependenciesTag = XmlTag("dependencies")
        ivyModuleTag.addChild(dependenciesTag)
        dependenciesTag.maybeAddAttribute("defaultconf", defaultConf)
        dependenciesTag.maybeAddAttribute("defaultconfmapping", defaultConfMapping)
        IvyDependencies(dependenciesTag).init()
    }

    fun dumpXml(): File {
        if (lastIvyModuleRevId != ivyModuleTag.revId) {
            val writer = FileWriter(ivyFile)
            writer.write(ivyModuleTag.toString())
            writer.append('\n')
            writer.close()
            lastIvyModuleRevId = ivyModuleTag.revId
        }
        return ivyFile
    }
}

public class IvyDependencies(val dependenciesTag: XmlTag) {
    public fun dependency(org: String? = null,
                          name: String,
                          branch: String? = null,
                          rev: String,
                          revConstraint: String? = null,
                          force: Boolean? = null,
                          conf: String? = null,
                          transitive: Boolean? = null,
                          changing: Boolean? = null
    ) {
        val dependencyTag = XmlTag("dependency")
        dependenciesTag.addChild(dependencyTag)
        dependencyTag.maybeAddAttribute("org", org)
        dependencyTag.addAttribute("name", name)
        dependencyTag.maybeAddAttribute("branch", branch)
        dependencyTag.addAttribute("rev", rev)
        dependencyTag.maybeAddAttribute("revConstraint", revConstraint)
        dependencyTag.maybeAddAttribute("force", force)
        dependencyTag.maybeAddAttribute("conf", conf)
        dependencyTag.maybeAddAttribute("transitive", transitive)
        dependencyTag.maybeAddAttribute("changing", changing)
    }
}

public class IvyModule(val file: File) {
    private val ivy = Ivy.newInstance()!!
    private var md: ModuleDescriptor? = null
    {
        ivy.configureDefault()
    }

    public fun resolve() {
        val report = ivy.resolve(file)!!
        md = report.getModuleDescriptor()!!
    }

    public fun retrieve(retrievePattern: String = "lib/[artifact]-[revision](-[classifier]).[ext]") {
        if (md == null) {
            resolve()
        }
        ivy.retrieve(md!!.getModuleRevisionId(), retrievePattern, RetrieveOptions())
    }
}

public fun ivyModule(init: IvyModuleDescriptor.() -> Unit): IvyModule {
    val md = IvyModuleDescriptor()
    md.init()
    return IvyModule(md.dumpXml())
}
