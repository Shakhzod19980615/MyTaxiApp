plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id ("org.jetbrains.kotlin.plugin.serialization")
    //id("com.mapbox.maps")
}

android {
    namespace = "com.example.mytaxiapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mytaxiapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    //noinspection UseTomlInstead
    implementation("androidx.compose.material3:material3")
    implementation(libs.material)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    implementation(libs.play.services.maps)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    //Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("com.google.dagger:dagger-android-support:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    //implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha02")
    kapt ("androidx.hilt:hilt-compiler:1.0.0-alpha02")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")
// AndroidX and Compose dependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("com.mapbox.mapboxsdk:mapbox-android-sdk:8.6.7")

    //implementation ("com.mapbox.maps:compose:9.2.6")
    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    //room
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.room:room-common:2.6.1")

    //Glide
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    kapt ("com.github.bumptech.glide:compiler:4.13.0")

    implementation ("com.google.android.gms:play-services-location:20.0.0")
    implementation("com.mapbox.mapboxsdk:mapbox-android-sdk:8.6.7") {
        exclude( "com.google.android.gms","play-services-location")
    }// Use the latest stable version
    implementation ("androidx.compose.runtime:runtime:1.5.0")

    //lifecycle
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")

    //Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //livedata
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    //lottieAnimation
    api ("com.airbnb.android:lottie:3.5.0")
    //fragment
    //ViewPagerIndicator
    //implementation("com.tbuonomo.viewpagerdotsindicator:viewpagerdotsindicator:5.0")
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    //ViewPager
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    //indicator
    implementation ("me.relex:circleindicator:2.1.6")

    //SocialLogin
    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.gms:google-services:4.4.2")
    //CardView
    //implementation("androidx.card-view:card-view:1.0.0")
}
kapt {
    correctErrorTypes = true
}