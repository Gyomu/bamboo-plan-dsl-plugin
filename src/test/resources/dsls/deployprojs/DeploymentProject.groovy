package dsls.deployprojs

project("SIMPLEPROJECT") {
    name "simple project"

    plan("SIMPLEPLAN") {
        name "simple plan"

        deploymentProject("dp1") {
            description "desc1"
            useCustomPlanBranch "develop"

            environment("env1") {
                description "desc"
            }
        }

        deploymentProject(name: "dp2") {
            description "desc1"
            useCustomPlanBranch "release"

            environment(name: "env1") {
                description "desc1"
            }
            environment("env2") {
                description "desc2"
            }
        }

    }
}