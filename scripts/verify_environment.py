import os
import shutil
import subprocess
import sys

def check_command(cmd, name):
    found = shutil.which(cmd)
    if found:
        print(f"  [OK] {name}: {found}")
        return True
    else:
        print(f"  [MISSING] {name} ('{cmd}' not found on PATH)")
        return False

def main():
    print("==========================================")
    print("Pocket Vision — Environment Verification")
    print("==========================================")

    python_ok = check_command("python", "Python Runtime")
    git_ok = check_command("git", "Git")
    java_ok = check_command("java", "Java Runtime")
    adb_ok = check_command("adb", "Android Debug Bridge (ADB)")

    android_home = os.getenv("ANDROID_HOME") or os.getenv("ANDROID_SDK_ROOT")
    if android_home and os.path.isdir(android_home):
        print(f"  [OK] Android SDK: {android_home}")
    else:
        # Check standard default location on Windows
        local_app_data = os.getenv("LOCALAPPDATA", "")
        default_sdk = os.path.join(local_app_data, "Android", "Sdk")
        if os.path.isdir(default_sdk):
            print(f"  [OK] Android SDK (Default): {default_sdk}")
        else:
            print("  [WARNING] Android SDK path not automatically detected.")

    print("\nVerification complete.")

if __name__ == "__main__":
    main()
