# beeapi
API for FRC Team 5183's inventory system.

# Download
Download a stable build from [here](https://github.com/frc5183/beeapi/releases).

Download a bleeding edge build from [here](https://github.com/frc5183/beeapi/actions/workflows/jar.yml?query=is%3Acompleted).
Click the most recent workflow run and download BeeAPI.jar found in the artifacts tab.

# TODO
- [X] Full Multithreading
- [X] Allow administrators to modify, add, remove users.
- [ ] Test everything and make sure it actually works.
- [X] Add offset to inventory items.
- [X] Add offset to get all users.
- [X] MULTITHREADING: Redo DatabaseRequestRunnable to make a new thread for each request in the cache/wait pool.
- [X] MULTITHREADING: Allow new types of custom runnables (e.g. ONESHOT, REPEATED, etc. and only stop ones that are REPEATED and force stop ONESHOT when it takes to long).\
- [X] MULTITHREADING: Instead of trying all kind of PreparedStmt, we only do PreparedQuery and for update and insert we just take the Entity? 
- [X] Verify if an entity is actually valid.
- [ ] Add a config file instead of ENV vars.