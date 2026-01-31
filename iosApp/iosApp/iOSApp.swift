import SwiftUI
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {

        // 1. Inicializa P07 (usa el archivo GoogleService-Info.plist por defecto)
        FirebaseApp.configure()
        print("✅ Firebase: P07 configurada (Default)")

        // 2. Inicializa P08 usando el archivo renombrado
        // Importante: El nombre "P08" debe coincidir con el que pidas en Kotlin
        if let path = Bundle.main.path(forResource: "GoogleService-Info-P08", ofType: "plist"),
           let options = FirebaseOptions(contentsOfFile: path) {
            FirebaseApp.configure(name: "P08", options: options)
            print("✅ Firebase: P08 configurada correctamente desde .plist")
        } else {
            print("❌ Firebase: No se pudo encontrar GoogleService-Info-P08.plist")
        }

        return true
    }
}

@main
struct IOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}