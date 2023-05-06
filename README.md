# Sign Language Detection App
[![Android CICD](https://github.com/andrezg98/SignLanguageDetectionApp/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/andrezg98/SignLanguageDetectionApp/actions/workflows/android.yml)

***Master Thesis** - Aplicación móvil didáctica para el aprendizaje del lenguaje de signos mediante la integración de un modelo de IA*

This repository present the contents developed for my Master Thesis, titled: ***"Aplicación móvil didáctica para el aprendizaje del lenguaje de signos mediante la integración de un modelo de IA"***. Mainly, the project code for the Android application developed.

Application Video 

https://user-images.githubusercontent.com/47974850/179364214-5ced89b9-ec24-4eaa-8b7c-8ab4575a6726.mov

## Table of Contents

  * [Application Screenshots](#application-screenshots)
  * [Android Project Structure](#android-project-structure)
  * [AI Model (Sign Language Detector) Development](#ai-model-sign-language-detector-development)
  * [Getting Started - Application Installation](#getting-started---application-installation)
    * [Prerequisites](#application-screenshots)
    * [Install via Android Studio](#install-via-android-studio-compilation)
    * [Manual APK Installation](#install-via-apk)
  * [Application Project CICD](#application-project-cicd)
  * [Planned features (future work development)](#application-screenshots)


## Application Screenshots

<img src="https://github.com/andrezg98/SignLanguageDetectionApp/blob/main/repoAssets/SCREENSHOTS.png?raw=true" width="1000" />


## Android Project Structure

This application project file structure is explained as follows:

```
.
├── main
│   ├── AndroidManifest.xml
│   ├── assets/: Folder containing AI tensorflow model and labels file.
│   │   ├── detect_sign_V4B_meta.tflite
│   │   └── labelmap_signs.txt
│   ├── java/
│   │   └── com/
│   │       └── andreaziqing/
│   │           └── signlanguagedetectionapp/
│   │               ├── Authentication/: Firebase Auth -related code.
│   │               ├── Database/: Firestore DB Wrapper and Session management code
│   │               ├── DetectionGames/: Lessons and Games main business logic code.
│   │               ├── Detector/: AI model recognition logic (Tensorflow Lite code).
│   │               ├── HelperClasses/: Adapters, view, environment and tracking helper classes.
│   │               ├── Navigation/: Navigation tabs controller code.
│   │               ├── OnBoarding/: Onboarding screen cards code.
│   │               └── Tabs/: Navigation tabs code.
│   └── res/: Resources folder with animation, drawables, app layout, images, etc.
│       ├── anim/: Button animation xml files.
│       ├── drawable/: Contains most images shown in games/lessons.
│       ├── layout/ : Contains app activity layout xml files
│       ├── menu/: nav view menu icons.
│       ├── mipmap/ folders: app icon files. 
│       ├── raw/ : Holds lottie .json files and sound effect files.
│       ├── values/: Localization strings and color/theme xml files.
└──androidTest/: test folder containing SignDetector detection tests.

```

## AI Model (Sign Language Detector) Development

<img src="https://github.com/andrezg98/SignLanguageDetectionApp/blob/main/repoAssets/mobile_net.png?raw=true" width="600" />

The folder `Detector Model Training and Export` contains the resources that have been developed for the Sign Language detection neural network that has been trained and exported to be used in this application.

Where:
 - [Training_and_TFLite_Model_Generation.ipynb](https://github.com/andrezg98/SignLanguageDetectionApp/blob/main/Detector%20Model%20Training%20and%20Export/Sign_Language_Detector_Training_and_TFLite_Model_Generation.ipynb): Python (Jupyter Notebook format) code that has been used for the model training and export to TFLite format.
 - [Writing_TFLIte_Metadata.ipynb](https://github.com/andrezg98/SignLanguageDetectionApp/blob/main/Detector%20Model%20Training%20and%20Export/Sign_Language_Detector_Adapting_TFLite_Model_Metadata.ipynb): Python (Jupyter Notebook format) code that has been used for the model tflite metadata write so that it could be later used in the Android application.
 
The resulting trained model file exported to TFLite as well as its "labels" .txt file containing the class names that the model has been trained on to detect can be found both in the application `assets` [folder](https://github.com/andrezg98/SignLanguageDetectionApp/tree/main/app/src/main/assets) , as `detect_sign_V4B_meta.tflite` and `labels.txt`

 
To achieve a sign language detection AI model, a MobileNet SSD V2 neural network has been fine tuned (trained) over this [a dataset of 1728 labeled images](https://public.roboflow.com/object-detection/american-sign-language-letters/1), and its resulting model has been exported to a Tensorflow Lite model format that has been integrated into a generic object detection framework in [Android](https://github.com/tensorflow/examples/tree/3c3806673635b702d5ce936f6f2235b84a937777/lite/examples/object_detection/android).

<img src="https://github.com/andrezg98/SignLanguageDetectionApp/blob/main/repoAssets/datasetSample.png?raw=true" width="800" />

The main code regarding the "low level" TensorflowLite model integration in Java/Android has been obtained from the [TFLite Official Object Detection sample](https://github.com/tensorflow/examples/tree/3c3806673635b702d5ce936f6f2235b84a937777/lite/examples/object_detection/android); more specifically from the "lib_task_api" API and the "tensorflow/lite/examples/detection/" sections; those regarding the `customview/`,  `env/`,  `tracking/` helpers as well as the `CameraActivity` and `DetectorActivity` core API classes.

## Getting Started - Application Installation

### Prerequisites
- Android device with at least **Android 8** version (API Level 26).
- Android Studio (if chose to install via Android Studio compilation).

You can install the application on your android smartphone in two ways:

### Install via Android Studio compilation

1. Clone this repository into a local folder of your computer with `git clone https://github.com/andrezg98/SignLanguageDetectionApp.git`
2. Load the project into Android Studio.
3. Build and Install the application to your smartphone provided is connected to your computer and Android Studio has connection to it (i.e. appears as a Build target).

### Install via APK

You can find the latest `.apk` file with the compiled application [in the Releases section](https://github.com/andrezg98/SignLanguageDetectionApp/releases) of the repository, or from the Latest [CICD build](https://github.com/andrezg98/SignLanguageDetectionApp/actions) artifact. 

## Application Project CI/CD

This project has a **Github Actions CI/CD pipeline** configured with two main stages:

1. Build and Test
2. Build and Upload APK File as Artifact.

You can find the .yml containing the pipeline and details in the [Actions](https://github.com/andrezg98/SignLanguageDetectionApp/actions) section of the Github Action.

<img src="https://github.com/andrezg98/SignLanguageDetectionApp/blob/main/repoAssets/cicd.png?raw=true" width="800" />


## Planned features (future work development)

- [ ] Add support for Spanish Sign Language.
- [ ] Configuration / Settings for app personalization.
- [ ] Social features: friend list, multiplayer challenges.
- [ ] Create further unit tests.
"# sign-language-detection-app--final-year-project" 
"# sign-language-detection-app--final-year-project" 
