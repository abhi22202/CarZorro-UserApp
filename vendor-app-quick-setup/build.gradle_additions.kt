// Add these dependencies to app/build.gradle.kts

dependencies {
    // Firebase Cloud Messaging
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // LocalBroadcastManager (for UI updates)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
}

// Add plugin at top if not already added
plugins {
    id("com.google.gms.google-services")
}


