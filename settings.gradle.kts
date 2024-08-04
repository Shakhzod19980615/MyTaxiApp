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
        jcenter()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = providers
                    .gradleProperty("pk.eyJ1Ijoic2hha2h6b2RiZWsxOTk4IiwiYSI6ImNsemQ4cWxrazBpMngya3FxeXVoaGI1ZTEifQ.kGdOBXEzaQgz35yP9bDGHw").orNull


            }
        }
    }
}

rootProject.name = "MyTaxiApp"
include(":app")
 