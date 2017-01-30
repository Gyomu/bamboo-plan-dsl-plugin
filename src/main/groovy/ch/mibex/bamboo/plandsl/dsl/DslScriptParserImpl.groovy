package ch.mibex.bamboo.plandsl.dsl

import ch.mibex.bamboo.plandsl.dsl.branches.Branch
import ch.mibex.bamboo.plandsl.dsl.branches.Branches
import ch.mibex.bamboo.plandsl.dsl.dependencies.Dependencies
import ch.mibex.bamboo.plandsl.dsl.notifications.EnvironmentNotifications
import ch.mibex.bamboo.plandsl.dsl.notifications.Notifications
import ch.mibex.bamboo.plandsl.dsl.scm.ScmCvs
import ch.mibex.bamboo.plandsl.dsl.scm.ScmType
import ch.mibex.bamboo.plandsl.dsl.tasks.DeployPluginTask
import ch.mibex.bamboo.plandsl.dsl.tasks.InjectBambooVariablesTask
import ch.mibex.bamboo.plandsl.dsl.tasks.ScriptTask
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper

class DslScriptParserImpl implements DslScriptParser {
    private final BambooFacade bambooFacade
    private Map<String, Object> exportedBambooObjects

    DslScriptParserImpl(BambooFacade bambooFacade) {
        this.bambooFacade = bambooFacade
    }

    DslScriptParserImpl() {
        this(new NullBambooFacade())
    }

    DslScript parse(DslScriptContext scriptContext) {
        CompilerConfiguration config = createCompilerConfig()
        try {
            DslScript script = parseScript(config, scriptContext)
            evaluateScript(script)
        } catch (CompilationFailedException e) {
            throw new DslException(e.message, e)
        } catch (GroovyRuntimeException e) {
            throw new DslScriptException(e.message, e)
        }
    }

    private DslScript evaluateScript(DslScript script) {
        script.setBambooFacade(bambooFacade)
        script.run()
        script
    }

    @SuppressWarnings('Instanceof')
    private DslScript parseScript(CompilerConfiguration config, DslScriptContext scriptContext) {
        ClassLoader parentClassLoader = DslScriptParserImpl.classLoader
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(parentClassLoader, config)
        Binding binding = createBinding()

        Script script
        if (scriptContext.body) {
            Class clazz = groovyClassLoader.parseClass(scriptContext.body)
            script = InvokerHelper.createScript(clazz, binding)
        } else {
            def engine = new GroovyScriptEngine(scriptContext.urlRoot as URL[], groovyClassLoader)
            script = engine.createScript(scriptContext.location, binding)
        }

        assert script instanceof DslScript
        script as DslScript
    }

    private Binding createBinding() {
        Binding binding = new Binding()
        binding.setVariable('out', bambooFacade.buildLogger)
        binding.setVariable('bamboo', bambooFacade.variableContext)

        exportedBambooObjects.each { key, value ->
            binding.setVariable(key, value)
        }

        binding
    }

    private static CompilerConfiguration createCompilerConfig() {
        def config = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        config.scriptBaseClass = DslScript.name
        def importCustomizer = new ImportCustomizer()
        // we need to embed these enums in files of the DSL and not in separate files because otherwise lookup
        // does not work in IDEs:
        importCustomizer.addStaticImport(Notifications.name, Notifications.NotificationEvent.simpleName)
        importCustomizer.addStaticImport(
                EnvironmentNotifications.name, EnvironmentNotifications.EnvironmentNotificationEvent.simpleName
        )
        importCustomizer.addStaticImport(
                InjectBambooVariablesTask.name, InjectBambooVariablesTask.VariablesScope.simpleName
        )
        importCustomizer.addStaticImport(Branch.name, Branch.NotifyOnNewBranchesType.simpleName)
        importCustomizer.addStaticImport(Branches.name, Branches.NewPlanBranchesTriggerType.simpleName)
        importCustomizer.addStaticImport(DeployPluginTask.name, DeployPluginTask.ProductType.simpleName)
        importCustomizer.addStaticImport(ScmType.name, ScmType.MatchType.simpleName)
        importCustomizer.addStaticImport(ScmCvs.name, ScmCvs.CvsModuleVersion.simpleName)
        importCustomizer.addStaticImport(ScriptTask.name, ScriptTask.ScriptInterpreter.simpleName)
        importCustomizer.addStaticImport(Dependencies.name, Dependencies.DependencyBlockingStrategy.simpleName)

        config.addCompilationCustomizers(importCustomizer)
        // would not allow usage of variables like bamboo or configure block:
        // config.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked))
        config
    }

}
