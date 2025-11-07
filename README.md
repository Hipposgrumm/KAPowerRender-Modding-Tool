# KAPowerRender Modding Tool
This is a tool for modding games made in Knowledge Adventure's (AKA JumpStart Games') in-house engine (AKA KAEngine), which is a fork of the PowerRender game engine.

The engine's file structure contains all of its assets in `.kar` files (probably stands for KAResource).
The files are formatted in a block-based structure that allows blocks of unknown types to be skipped.
Anything that is not implemented will remain intact when the modded `KAR` is generated.
## Currently Working
- Textures (both TxFm and TrFm block types)
- Audio (replacements may occasionally result in crashes and playback is a donkey)
## Currently Needs Improvement
- World Objects - Technically working, but good luck figuring out what's what.
## Not Implemented
- Scripts
- Models/Prefabs
- Interfaces (the ones the game uses)
- Whatever `Tb` means
- Unique types used by 3DVW
- Anything else not listed here

# How to Use
Go to [releases](https://github.com/Hipposgrumm/KAPowerRender-Modding-Tool/releases) and download `app.zip`.  
Unzip `app.zip`, navigate to the `bin` folder inside the extracted folder, and run `app.bat`. It's hidden among the other files but it's there.
Opening a `KAR` file with the app or dragging one on top of it will show you its contents.
