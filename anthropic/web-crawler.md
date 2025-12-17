# Web Crawler


## Basic Problem

You are given:
- A starting URL `startUrl`
- An interface `HtmlParser` that can fetch all URLs from a given web page

Implement a web crawler that **returns all URLs** reachable from `startUrl` that **share the same hostname** as `startUrl`.
The order of URLs in the result does not matter.

Below is the interface for HtmlParser:
```java
interface HtmlParser {
    // Returns all URLs from a given page URL.
    public List<String> getUrls(String url);
}
```

Your function will be called like `List<String> crawl(String startUrl, HtmlParser htmlParser)`.


### Requirements

Your crawler should:
1. **Start** from `startUrl`.  
2. **Use** `HtmlParser.getUrls(url)` to obtain all links from a page.  
3. **Avoid duplicates** — never crawl the same URL twice.  
4. **Restrict scope** — only follow URLs whose **hostname** matches that of `startUrl`.  
5. Assume all URLs use the `http` protocol and do **not** include a port.


### Note for Candidate

- **URL Fragments**: Consider how to handle URL fragments (e.g., `http://example.com/page#section1`). Should these be treated as the same URL or different? Clarify with the interviewer if needed.
- **URL Normalization**: Assume URL normalization is NOT required for the basic problem.


## Follow-up: Multithreaded/Concurrent Implementation (Important!!)

After implementing the basic single-threaded version, implement a **multithreaded or concurrent version** of the web crawler to improve performance.

### Requirements

1. **Parallelize the crawling** — multiple URLs should be fetched concurrently
2. **Thread safety** — ensure no race conditions when accessing shared data structures (visited set, result list)
3. **Avoid duplicates** — even with multiple threads, each URL should only be crawled once
4. **Maintain hostname restriction** — only crawl URLs with matching hostnames

### Note for Candidate

Use a thread pool to control the total number of threads.

- Do NOT create unbounded threads (e.g., one thread per URL)
- A thread pool limits concurrency and prevents resource exhaustion
- Common approach: Use a fixed-size thread pool (e.g., 10-20 threads) with a task queue


## Solution: JavaScript Multithreaded Implementation

Below is a complete JavaScript implementation of the multithreaded web crawler with proper concurrency control and URL normalization.

### ThreadPool Implementation

```javascript
class ThreadPool {
    constructor(maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        this.queue = [];
        this.runningTasks = 0;
        this.waitForFinishPromise = null;
        this.waitForFinishResolve = null;
    }

    // Add a task to the queue and start processing
    execute(task) {
        return new Promise((resolve, reject) => {
            this.queue.push({
                task,
                resolve,
                reject
            });
            this._processQueue();
        });
    }

    _processQueue() {
        // Don't start new tasks if at max concurrency
        if (this.runningTasks >= this.maxConcurrency) {
            return;
        }

        // If queue is empty, check if all work is done
        if (this.queue.length === 0) {
            if (this.runningTasks === 0) {
                this.waitForFinishResolve && this.waitForFinishResolve();
                this.waitForFinishPromise = null;
                this.waitForFinishResolve = null;
            }
            return;
        }

        // Execute next task from queue
        const { task, resolve, reject } = this.queue.shift();
        this.runningTasks++;
        Promise.resolve()
            .then(task)
            .then(resolve, reject)
            .finally(() => {
                this.runningTasks--;
                this._processQueue();
            });
    }

    // Returns a promise that resolves when all tasks finish
    waitForFinish() {
        if (this.runningTasks === 0 && this.queue.length === 0) {
            return Promise.resolve();
        }

        if (!this.waitForFinishPromise) {
            this.waitForFinishPromise = new Promise((resolve) => {
                this.waitForFinishResolve = resolve;
            });
        }
        return this.waitForFinishPromise;
    }
}
```

### URL Normalization Helper

```javascript
function getProcessedUrlObject(url) {
    const newUrl = new URL(url);
    // Remove hash fragments to treat http://example.com/page#section1 
    // and http://example.com/page#section2 as the same URL
    newUrl.hash = "";
    return newUrl;
}
```

### Main Crawler Function

```javascript
async function crawl(startUrl, htmlParser, maxConcurrency = 10) {
    const processedStartUrlObject = getProcessedUrlObject(startUrl);
    const startHostName = processedStartUrlObject.hostname;

    const result = [processedStartUrlObject.toString()];
    const visited = new Set([processedStartUrlObject.toString()]);
    const pool = new ThreadPool(maxConcurrency);

    const crawlUrl = async (url) => {
        try {
            const nextUrls = await htmlParser.getUrls(url);
            for (const nextUrl of nextUrls) {
                const nextUrlProcessedObject = getProcessedUrlObject(nextUrl);
                const nextUrlProcessedString = nextUrlProcessedObject.toString();
                
                // Only crawl if same hostname and not yet visited
                if (nextUrlProcessedObject.hostname === startHostName 
                    && !visited.has(nextUrlProcessedString)) {
                    visited.add(nextUrlProcessedString);
                    result.push(nextUrlProcessedString);
                    // Queue the URL for crawling (fire and forget)
                    pool.execute(() => crawlUrl(nextUrl)).catch((err) => {
                        console.error(`Error crawling ${nextUrl}:`, err);
                    });
                }
            }
        } catch (error) {
            console.error(`Error fetching URLs from ${url}:`, error);
        }
    };

    // Start crawling from the initial URL
    await pool.execute(() => crawlUrl(startUrl));
    // Wait for all queued tasks to complete
    await pool.waitForFinish();
    
    return result;
}
```

### Usage Example

```javascript
// Example with HtmlParser mock
class HtmlParser {
    constructor(urls) {
        this.urls = urls;
    }

    async getUrls(url) {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, Math.random() * 15));
        return this.urls[url] || [];
    }
}

const urls = {
    "http://news.yahoo.com": [
        "http://news.yahoo.com/news/topics/",
        "http://news.yahoo.com/news"
    ],
    "http://news.yahoo.com/news/topics/": [
        "http://news.yahoo.com/news",
        "http://news.yahoo.com/news/sports"
    ],
    "http://news.yahoo.com/news": [
        "http://news.google.com"
    ],
    "http://news.yahoo.com/news/sports": []
};

const parser = new HtmlParser(urls);
const result = await crawl("http://news.yahoo.com", parser, 2);
console.log("Crawled URLs:", result);
// Expected output: All yahoo.com URLs (excluding news.google.com)
```

### Key Design Decisions

1. **ThreadPool**: Implements a custom async task queue with configurable max concurrency
2. **URL Normalization**: Removes hash fragments to avoid treating the same page as different URLs
3. **Thread Safety**: Uses a `Set` for `visited` tracking; JavaScript's single-threaded event loop ensures no race conditions
4. **Error Handling**: Catches and logs errors without crashing the entire crawl
5. **Concurrency Control**: Limits parallel requests to avoid overwhelming servers


## Additional Discussion Topics

The following questions are for verbal discussion. The interviewer does not expect you to code solutions, but be prepared to explain your approach and reasoning.


### 1. Concurrency Models: Threads vs Processes

**Question:** What are the differences between threads and processes? When would you use one over the other for web crawling?

**Discussion Points:**
- **Threads**: Lightweight, share memory, lower overhead, suitable for I/O-bound tasks like web crawling
- **Processes**: Heavyweight, isolated memory, better for CPU-bound tasks, avoid GIL issues in Python
- **Trade-offs**: Memory usage, communication overhead, fault isolation
- For web crawling: Threads are typically preferred due to I/O-bound nature and shared state needs


### 2. Distributed Crawling System

**Question:** Our list of seed URLs is now in the millions, and a single machine can't handle the workload. How would you design a distributed crawling system across multiple machines?

**Discussion Points:**
- **URL Distribution**: How to partition and assign URLs to different machines
  - Consistent hashing based on hostname/domain
  - Central coordinator/queue system
  - Each machine owns specific domains
- **Coordination**: How machines communicate and avoid duplicates
  - Shared distributed cache (Redis, Memcached)
  - Central URL frontier/queue
  - Partition by URL hash
- **Load Balancing**: Ensuring even distribution of work
- **Fault Tolerance**: Handling machine failures and retries


### 3. Politeness Policy and Rate Limiting

**Question:** Our current crawler is too aggressive and might overload the servers we are crawling. How would you implement a politeness policy to ensure we don't send too many requests in a short period?

**Discussion Points:**
- **robots.txt compliance**: Respect crawl delays and disallowed paths
- **Rate limiting per domain**: Limit requests per second to each hostname
  - Per-domain request queues
  - Delay between requests to same domain (e.g., 1-2 seconds)
- **Adaptive throttling**: Slow down if server responds slowly or returns errors
- **Distributed rate limiting**: Coordinate across multiple crawler machines
- **Time-based scheduling**: Crawl during off-peak hours if possible


### 4. Duplicate Content Detection

**Question:** We are finding that many different URLs point to the same or very similar content (mirrors, URL parameters, etc.). How would you detect and handle this to save on storage and processing?

**Discussion Points:**
- **Content Fingerprinting**:
  - Hash the page content (MD5, SHA-256)
  - Store hash to detect exact duplicates
  - Simhash for near-duplicate detection
- **URL Normalization**:
  - Canonicalize URLs (remove tracking parameters, sort query params)
  - Follow canonical tags in HTML
- **Content Similarity**:
  - Shingling/n-grams for fuzzy matching
  - Jaccard similarity
- **Trade-offs**:
  - Storage cost vs computation cost
  - False positives vs false negatives
  - When to fetch vs when to skip