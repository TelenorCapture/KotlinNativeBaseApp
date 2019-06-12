import UIKit
import baseLib

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        print("Platform name: ", PlatformKt.platformName())
        print("JSON: ", TestKt.generateJson())
    }
}
