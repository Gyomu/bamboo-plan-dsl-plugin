package dsls.triggers

project("BITBUCKETSERVER") {
    name "Simple project"

    plan("BITBUCKETSERVER") {
        name "Simple plan"
        description "this is a simple plan"
        enabled true

        triggers {
            bitbucketServerRepositoryTriggered("run when new code") {
            }
        }

        stage("simple stage") {
            description "this is a simple stage"
            manual false

            job("SIMPLEJOB") {
                name "Simple job"
                description "This is a simple job"
                enabled true

                tasks {
                    checkout("checkout default repo") {
                        forceCleanBuild true

                        repository("myBitbucketServerRepo") {
                            checkoutDirectory "a/b"
                        }

                    }
                }
            }
        }

        scm {
            bitbucketServer("myBitbucketServerRepo") {
                projectKey "PROJECT_1"
                repoSlug "rep_1"
                branch "master"
                serverName "Bitbucket"
                repoId "1"
                repositoryUrl "ssh://git@localhost:7999/project_1/rep_1.git"
            }
        }
    }
}