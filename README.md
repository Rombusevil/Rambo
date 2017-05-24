# Rambo

WIP

## Use
1. Create a LibGDX gradle project.
1. Open project's build.gradle and add the following under _allprojects_ => _repositories_ 

```gradle
  maven { url "https://jitpack.io" }
```
3. Also add under _project(":core")_ => _dependencies_:
```gradle
  compile 'com.github.Rombusevil:Rambo:master'
```
