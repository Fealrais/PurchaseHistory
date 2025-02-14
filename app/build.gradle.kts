import com.android.sdklib.AndroidVersion

plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.angelp.purchasehistory"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.angelp.purchasehistory"
        minSdk = 29
        targetSdk = AndroidVersion.VersionCodes.TIRAMISU
        versionCode = 5
        versionName = "1.0.0"
        testInstrumentationRunner = "com.angelp.purchasehistory.setup.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(fileTree("libs"))
    // Fix Duplicate class
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.paging:paging-runtime:3.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")

//    QR reading
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.opencsv:opencsv:4.6")
//    Web
    implementation("com.squareup.okhttp3:okhttp-testing-support:3.14.9")
    implementation("com.google.code.gson:gson:2.10")
    implementation("dev.gustavoavila:java-android-websocket-client:2.0.1")

//  Hilt Dependency injection
    implementation("com.google.dagger:hilt-android:2.44.2")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
//  UI
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("com.github.Mohammad3125:KavehColorPicker:v1.0.0")
    implementation ("uk.co.samuelwall:material-tap-target-prompt:3.0.0")

    implementation("org.aspectj:aspectjweaver:1.9.22.1")

    // Chart and graph library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.tradingview:lightweightcharts:3.8.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.paging:paging-guava:3.3.2")
//    implementation("com.google.android.gms:play-services-ads:23.4.0")

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    annotationProcessor("com.google.dagger:hilt-compiler:2.44.2")

// Hilt Jetpack Integrations
//    implementation 'androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03'
    annotationProcessor("androidx.hilt:hilt-compiler:1.2.0")


//    Lombok
    //noinspection AnnotationProcessorOnCompilePath
    implementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.dagger:hilt-android-testing:2.44")
    testAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.44")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    androidTestAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.44")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}