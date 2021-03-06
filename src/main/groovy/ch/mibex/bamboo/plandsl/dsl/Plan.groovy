package ch.mibex.bamboo.plandsl.dsl

import ch.mibex.bamboo.plandsl.dsl.branches.Branches
import ch.mibex.bamboo.plandsl.dsl.dependencies.Dependencies
import ch.mibex.bamboo.plandsl.dsl.deployprojs.DeploymentProject
import ch.mibex.bamboo.plandsl.dsl.notifications.Notifications
import ch.mibex.bamboo.plandsl.dsl.permissions.Permissions
import ch.mibex.bamboo.plandsl.dsl.plans.Miscellaneous
import ch.mibex.bamboo.plandsl.dsl.scm.Scm
import ch.mibex.bamboo.plandsl.dsl.triggers.Triggers
import ch.mibex.bamboo.plandsl.dsl.variables.Variables
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields=true, excludes = ['metaClass'])
@ToString(includeFields=true)
class Plan extends BambooObject {
    private String key
    private String name
    private String description
    private Scm scm = new Scm(bambooFacade)
    private boolean enabled = true
    private List<Stage> stages = []
    private List<DeploymentProject> deploymentProjects = []
    private Triggers triggers = new Triggers(bambooFacade)
    private Branches branches = new Branches(bambooFacade)
    private Notifications notifications = new Notifications(bambooFacade)
    private Variables variables = new Variables(bambooFacade)
    private Dependencies dependencies = new Dependencies(bambooFacade)
    private Permissions permissions = new Permissions(bambooFacade)
    private Miscellaneous miscellaneous = new Miscellaneous(bambooFacade)

    // for testing
    protected Plan() {}

    /**
     * @param key the key of the plan consisting of an uppercase letter followed by one or more uppercase
     * alphanumeric characters
     * @deprecated use {@link #Plan(String, String, BambooFacade)} instead
     */
    @Deprecated
    protected Plan(String key, BambooFacade bambooFacade) {
        super(bambooFacade)
        planKey(key)
    }

    /**
     * Specifies the key of the plan.
     *
     * @param key the key of the plan consisting of an uppercase letter followed by one or more uppercase
     * alphanumeric characters
     * @param name the name of the build plan
     */
    protected Plan(String key, String name, BambooFacade bambooFacade) {
        super(bambooFacade)
        planKey(key)
        planName(name)
    }

    private void planKey(String key) {
        Validations.requireNotNullOrEmpty(key, 'plan key must be specified')
        Validations.requireTrue(
                key ==~ /[A-Z][A-Z0-9]*/,
                'key must consist of an uppercase letter followed by one or more uppercase alphanumeric characters.'
        )
        this.key = key
    }

    /**
     * Specifies the name of the plan.
     *
     * @deprecated use {@link #Plan(String, String, BambooFacade)} instead
     */
    @Deprecated
    void name(String name) {
        planName(name)
    }

    private void planName(String name) {
        Validations.requireNotNullOrEmpty(name, 'plan name must be specified')
        this.name = name
    }

    /**
     * Specifies the description of the plan.
     */
    void description(String description) {
        this.description = Validations.requireSafeBambooString(description)
    }

    /**
     * Specifies if the plan should be initially enabled or not.
     */
    void enabled(boolean enabled) {
        this.enabled = enabled
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param name the name of the deployment project
     * @since 1.1.0
     */
    DeploymentProject deploymentProject(
            String name,
            @DelegatesTo(value = DeploymentProject, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        def deploymentProject = new DeploymentProject(name, bambooFacade)
        DslScriptHelper.execute(closure, deploymentProject)
        deploymentProjects << deploymentProject
        deploymentProject
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param name the name of the deployment project
     * @param id the id of the deployment project
     * @since 1.6.1
     */
    DeploymentProject deploymentProject(
            String name, long id,
            @DelegatesTo(value = DeploymentProject, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        def deploymentProject = new DeploymentProject(name, id, bambooFacade)
        DslScriptHelper.execute(closure, deploymentProject)
        deploymentProjects << deploymentProject
        deploymentProject
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param params the parameters of the deployment project. Only "name" and "id" are supported.
     * @since 1.1.0
     */
    DeploymentProject deploymentProject(
            Map<String, Object> params,
            @DelegatesTo(value = DeploymentProject, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        //FIXME this can be improved once https://issues.apache.org/jira/browse/GROOVY-7956 is implemented
        if (params.containsKey('id')) {
            deploymentProject(params['name'] as String, checkDeploymentProjectId(params), closure)
        } else {
            deploymentProject(params['name'] as String, closure)
        }
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param params the parameters of the deployment project. Only "name" and "id" are supported.
     * @since 1.1.0
     */
    DeploymentProject deploymentProject(Map<String, Object> params) {
        if (params.containsKey('id')) {
            deploymentProject(params['name'] as String, checkDeploymentProjectId(params))
        } else {
            deploymentProject(params['name'] as String)
        }
    }

    private static Long checkDeploymentProjectId(Map<String, Object> params) {
        Validations.requireTrue(params['id'] ==~ /\d+/, 'deployment project ID must only consist of digits.')
        params['id'] as Long
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param name the name of the deployment project
     * @param id the id of the deployment project
     * @since 1.6.1
     */
    DeploymentProject deploymentProject(String name, long id) {
        def deploymentProject = new DeploymentProject(name, id, bambooFacade)
        deploymentProjects << deploymentProject
        deploymentProject
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param name the name of the deployment project
     * @since 1.1.0
     */
    DeploymentProject deploymentProject(String name) {
        def deploymentProject = new DeploymentProject(name, bambooFacade)
        deploymentProjects << deploymentProject
        deploymentProject
    }

    /**
     * Defines a deployment project for this plan. This can be called multiple times if you have multiple deployment
     * projects for this plan.
     *
     * @param name the name of the deployment project
     * @since 1.1.0
     */
    DeploymentProject deploymentProject(DeploymentProject deploymentProject) {
        deploymentProjects << deploymentProject
        deploymentProject
    }

    /**
     * Specifies the repositories for this plan.
     */
    Scm scm(@DelegatesTo(value = Scm, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        Scm scm = new Scm(bambooFacade)
        DslScriptHelper.execute(closure, scm)
        this.scm = scm
    }

    /**
     * Specifies the triggers for this plan.
     */
    Triggers triggers(@DelegatesTo(value = Triggers, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        Triggers triggers = new Triggers(bambooFacade)
        DslScriptHelper.execute(closure, triggers)
        this.triggers = triggers
    }

    /**
     * Specifies the branches for this plan.
     */
    Branches branches(@DelegatesTo(value = Branches, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        Branches branches = new Branches(bambooFacade)
        DslScriptHelper.execute(closure, branches)
        this.branches = branches
    }

    Branches branches() {
        branches = new Branches(bambooFacade)
        branches
    }

    /**
     * Specifies a stage for this plan. If your plan has multiple stages, call this multiple times.
     *
     * @param name the name of the stage
     */
    Stage stage(String name, @DelegatesTo(value = Stage, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        def stage = new Stage(name, bambooFacade)
        DslScriptHelper.execute(closure, stage)
        stages << stage
        stage
    }

    /**
     * Specifies a stage for this plan. If your plan has multiple stages, call this multiple times.
     *
     * @param stageParams the properties for the stage. Currently, only "name" is expected.
     */
    Stage stage(Map<String, String> stageParams,
                @DelegatesTo(value = Stage, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        //FIXME this can be improved once https://issues.apache.org/jira/browse/GROOVY-7956 is implemented
        stage(stageParams['name'], closure)
    }

    /**
     * Specifies a stage for this plan. If your plan has multiple stages, call this multiple times.
     *
     * @param stageParams the properties for the stage. Currently, only "name" is expected.
     */
    Stage stage(Map<String, String> stageParams) {
        //FIXME this can be improved once https://issues.apache.org/jira/browse/GROOVY-7956 is implemented
        stage(stageParams['name'])
    }

    Stage stage(Stage stage) {
        stages << stage
        stage
    }

    /**
     * Specifies a stage for this plan. If your plan has multiple stages, call this multiple times.
     *
     * @param name the name for the stage
     */
    Stage stage(String name) {
        def stage = new Stage(name, bambooFacade)
        stages << stage
        stage
    }

    /**
     * Specifies the notifications for this plan.
     */
    Notifications notifications(@DelegatesTo(value = Notifications, strategy = Closure.DELEGATE_FIRST) Closure c) {
        notifications = new Notifications(bambooFacade)
        DslScriptHelper.execute(c, notifications)
        notifications
    }

    Notifications notifications() {
        notifications = new Notifications(bambooFacade)
        notifications
    }

    /**
     * Specifies the miscellaneous options for this plan.
     */
    Miscellaneous miscellaneous(@DelegatesTo(value = Miscellaneous, strategy = Closure.DELEGATE_FIRST) Closure c) {
        def miscellaneous = new Miscellaneous(bambooFacade)
        DslScriptHelper.execute(c, miscellaneous)
        this.miscellaneous = miscellaneous
    }

    /**
     * Specifies the variables for this plan.
     */
    Variables variables(@DelegatesTo(value = Variables, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        def variables = new Variables(bambooFacade)
        DslScriptHelper.execute(closure, variables)
        this.variables = variables
    }

    /**
     * Specifies the dependencies for this plan.
     */
    Dependencies dependencies(@DelegatesTo(value = Dependencies, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        def dependencies = new Dependencies(bambooFacade)
        DslScriptHelper.execute(closure, dependencies)
        this.dependencies = dependencies
    }

    /**
     * Specifies the permissions for this plan.
     *
     * @since 1.5.1
     */
    Permissions permissions(@DelegatesTo(value = Permissions, strategy = Closure.DELEGATE_FIRST)  Closure closure) {
        def permissions = new Permissions(bambooFacade)
        DslScriptHelper.execute(closure, permissions)
        this.permissions = permissions
    }
}
