## Improved
- Videos start faster on mobile data: thumbnail downloads now share the connection more fairly with the video itself, so tapping a video is less likely to wait behind pictures
- The home feed stays fresher on mobile data with fewer requests: a refresh now trusts the channel feeds it already received and only opens channel pages for the channels those feeds could not reach
- The home feed scrolls more smoothly: the list is prepared off the main thread, and the stored feed is now indexed by upload time so the newest videos are found without sorting the whole store
