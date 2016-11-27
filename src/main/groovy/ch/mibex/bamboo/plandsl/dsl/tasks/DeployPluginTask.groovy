package ch.mibex.bamboo.plandsl.dsl.tasks

import ch.mibex.bamboo.plandsl.dsl.BambooFacade
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields=true)
@ToString(includeFields=true)
class DeployPluginTask extends Task {
    private static final TASK_ID = 'com.atlassian.bamboo.plugins.deploy.continuous-plugin-deployment:deploy-task'
    private ProductType productType
    private String deployURL
    private String deployUsername
    private String deployPasswordVariable
    private boolean deployBranchEnabled
    private boolean certificateCheckDisabled
    private boolean useAtlassianIdWebSudo
    private String deployArtifactName

    DeployPluginTask(BambooFacade bambooFacade) {
        super(bambooFacade, TASK_ID)
    }

    //for tests
    protected DeployPluginTask() {}

    /**
     * Select a Bamboo artifact to deploy. The artifact should be a single plugin jar file.
     */
    void deployArtifactName(String deployArtifactName) {
        this.deployArtifactName = deployArtifactName
    }

    /**
     * The Atlassian product type to deploy the artifact to.
     */
    void productType(ProductType productType) {
        this.productType = productType
    }

    /**
     * The address of the remote Atlassian application where the plugin will be installed.
     */
    void deployURL(String deployURL) {
        this.deployURL = deployURL
    }

    /**
     * User name to deploy.
     */
    void deployUsername(String deployUsername) {
        this.deployUsername = deployUsername
    }

    /**
     * A Bamboo variable with the password for this user to deploy.
     */
    void deployPasswordVariable(String deployPasswordVariable) {
        this.deployPasswordVariable = deployPasswordVariable
    }

    /**
     * Allow this task to execute on branch builds. This is disabled by default, since you generally only want a single
     * branch of development being deployed to a single environment.
     */
    void deployBranchEnabled(boolean deployBranchEnabled = true) {
        this.deployBranchEnabled = deployBranchEnabled
    }

    /**
     * Allow SSL verification errors. For example, you may find this useful when deploying to a staging environment
     * that uses a self-signed server certificate.
     */
    void certificateCheckDisabled(boolean certificateCheckDisabled = true) {
        this.certificateCheckDisabled = certificateCheckDisabled
    }

    /**
     * Use Atlassian web sudo.
     */
    void useAtlassianIdWebSudo(boolean useAtlassianIdWebSudo = true) {
        this.useAtlassianIdWebSudo = useAtlassianIdWebSudo
    }

    @Override
    protected Map<String, String> getConfig(Map<Object, Object> context) {
        Map<String, String> config = [:]
        config.put('bcpd.config.productType', productType.productKey)
        config.put('useAtlassianId', 'false')
        config.put('confDeployURL', deployURL)
        config.put('confDeployUsername', deployUsername)
        config.put('enableTrafficLogging', 'false')
        config.put('confDeployPasswordVariableCheck', 'true')
        config.put('confDeployPasswordVariable', deployPasswordVariable)
        config.put('deployBranchEnabled', deployBranchEnabled as String)
        config.put('certificateCheckDisabled', certificateCheckDisabled as String)
        config.put('multiProduct', 'true')
        config.put('atlassianIdPasswordVariableCheck', 'false')
        config.put('useAtlassianIdWebSudo', useAtlassianIdWebSudo as String)
        def contextArtifacts = context['artifacts']
        def artifact = contextArtifacts[deployArtifactName]
        if (artifact) {
            def info = artifact.asType(ArtifactInfo)
            def artifactKey = "v2:${info.artifactId}:${info.taskId}:${info.transferId}:${info.name}".toString()
            config.put('confDeployJar', artifactKey)
        }
        config
    }

    static enum ProductType {
        STASH('bcpd.product.stash'), BAMBOO('bcpd.product.bamboo')

        ProductType(String name) {
            this.productKey = name
        }

        String productKey
    }

}
