import java.util.Calendar

plugins {
    alias(libs.plugins.android.application)
}

val majorVersion = 1
val minorVersion = 0
val customVersionCode = calculateVersionCode()
android {
    namespace = "org.cosh.launchertv"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "org.cosh.launchertv"
        minSdk = 16
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // MY POSTBUILD TASK

    gradle.buildFinished {
        val versionJson = """
            {
              "latestVersion": "$majorVersion.$minorVersion.$customVersionCode",
              "version": $customVersionCode,
              "filename": "https://github.com/micha102/LauncherTV/releases",
              "releaseNotes": [
                "- Bug fixes"
              ]
            }
        """.trimIndent()
        val outputJsonFile = File(buildDir, "version.json")
        outputJsonFile.writeText(versionJson)
    }
}


dependencies {
    implementation(libs.appcompat)
}

// Function to calculate the version code
fun calculateVersionCode(): Int {
    // Get the current date in milliseconds
    val currentDateMillis = System.currentTimeMillis()
    // Subtract the base date (e.g., January 1, 2020) in milliseconds
    val baseDateMillis = getDateInMillis(2020, 0, 1)
    // Calculate the number of days since the base date
    val hoursSinceBaseDate = ((currentDateMillis - baseDateMillis) / (1000 * 60 * 60)).toInt()

    // Return the version code based on the number of days
    return hoursSinceBaseDate
}

// Function to get the date in milliseconds
fun getDateInMillis(year: Int, month: Int, day: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
