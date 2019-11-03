# TimedCache
Classes of this repository represent the high level caching mechanism which provides possibility to cache any objects in JVM memory for set time and also prevents OutOfMemoryError caused by such caching. Implemented two caching approaches: 1) all cached objects are handled by common daemon thread; 2) each cached object is handled by separate thread. For the most cases is better first approach - daemon thread which is common for all cached objects.