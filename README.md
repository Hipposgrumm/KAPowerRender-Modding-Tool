# KAPowerRender Modding Tool
This is a tool for modding games made in Knowledge Adventure (later known as JumpStart Games)'s in-house engine, internally named KAEngine, which is a fork of the PowerRender game engine.
<!-- I have to hit as many buzzwords as possible. -->

The engine's file structure contains all of its assets in `.kar` files (probably stands for KAResource).
The files are formatted in a block-based structure that allows blocks of unknown types to be skipped.
Anything that is not implemented will remain intact when the modded `KAR` is generated.

## Functionality
- ✅Math Blaster: Master the Basics
- ✅JumpStart 3D Virtual World 2nd Grade (Teacher's Edition)
- ❌Barbie Fashion Show
- ⚠️Any other 3D Virtual World game

<sub>✅Working - ❌Not Supported - ⚠️Untested (but should work)</sub>

# Features
## Currently Working
- Textures (both TxFm and TrFm block types)
- Audio (warning: replaced audio may cause the game to crash; also the playback is broken but the exported file is fine)
- Models (meshes only)
- World Objects (but nothing's labeled, so good luck figuring out what's what)
## Not Implemented
- Scripts
- HUD/UI Interfaces
- Whatever `Tb` means
- Unique types used by 3DVW
- Anything else not listed here
## Planned
- Search Feature
- Model Export
- "KAR Map" (a seperate or embedded file for user-defined labeling of file contents)
- Entire Level View/Export
- Hex Editor for unknown data
- Improve Viewer tool
- Read Barbie Fashion Show

# How to Use
Go to [releases](https://github.com/Hipposgrumm/KAPowerRender-Modding-Tool/releases) and download `KAPowerRenderModdingTool.zip`. Extract it somewhere, and inside a folder inside that, there is a file called `run.bat`. Run it.  
Opening a `KAR` file with the app or dragging one on top of it will show you its contents.

### How to use in IDE
<sup>*This is more of a self reference if anything.*</sup>  
Download the project files and open in an IDE of your choice (IntelliJ IDEA). This can be the latest on `master` or from the source files on a release.  
To run the project, run the `run` task (under `application`).  
To build it, run `jlinkZip` or `jlink` (under `build`).
