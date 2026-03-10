pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Clara Challenge"
include(":app")

// Core modules
include(":core:model")
include(":core:network")
include(":core:data")
include(":core:domain")
include(":core:common")

// Feature modules
include(":feature:search:api")
include(":feature:search:impl")
include(":feature:artist:api")
include(":feature:artist:impl")
include(":feature:discography:api")
include(":feature:discography:impl")
