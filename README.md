# Eco App (BMW MS Android)

This is the Android application for the BMW Medical Waste Management (BMW MS) project.

## Recent Features & Updates

1. **Mandatory Weighing Photo Capture:**
   - Removed weighing photo capture from the manual weight dialog.
   - Added a mandatory **Capture Weighing Photo** button and live preview UI (thumbnail, file size, Retake/Remove buttons) to the main scan fragment (`fragment_cbwtf_hcf_scan.xml`).
   - Added a sample weighing station photo reference link with `outline_info_24` icon allowing operators to view the standard setup guidelines.

2. **Batched Multipart API Submissions:**
   - Updated First Scan (`scanning_hcf_side`) and Rescan (`scanning_cbwtf_side`) workflows to submit all scanned items in a single batched multipart request.
   - Serializes all scanned bag items into a JSON array sent under the `"json"` part alongside the compressed JPEG weighing photo sent under `"weighing_photo"`.

3. **Reports & Weight Photo Viewer:**
   - Added HCF and CBWTF weight photo columns to the Reports listing (`ReportsAdapter` & `GetCbwtfReportsAdapter`).
   - Clicking the visibility eye icon (`ic_baseline_visibility_24`) opens a full-screen Material Design image viewer overlay (`dialog_image_viewer.xml`) with a loading progress indicator.

## Project Structure

The project follows a standard Android project structure with a single `:app` module.

## Tech Stack

- **Language:** Kotlin / Java
- **UI:** XML Layouts & Material Components
- **Networking:** OkHttp & Retrofit / Volley
- **Local Storage:** Room Database

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and build the project (`app:assembleDebug`).

## PlayStore Url
https://play.google.com/store/apps/details?id=com.appventurez.bmwms
