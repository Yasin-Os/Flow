## Improved
- Videos start faster on mobile data: the first picture now starts at a lower quality when the connection is still unknown, then steps up as the connection proves itself
- The player starts sooner on mobile data: it waits less for the first picture and steps the quality down after the first stall instead of the third
- The home feed shows its first videos sooner on first launch, after the first few uploads instead of waiting for a full page
- The home feed scrolls more smoothly: suggestions are picked without sorting the whole list first, and channel pictures are loaded only for the videos on screen
- Thumbnails use less mobile data: on mobile data they load at a smaller size, while Wi-Fi keeps the full size
- The app does less repeated work when you open it: the channel list is read once and reused, the safety filter is ready before the first feed load, and the app no longer re-checks channels that were just refreshed

## Fixed
- Fixed the player sometimes waiting on a quality upgrade that a slow connection could not use, so playback starts with the video that is already ready
