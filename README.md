# Multi-Thread Web Scraper

This is a multi-threaded web scraping project developed in Java. It is designed to concurrently extract content from web pages, utilizing a robust architecture to handle different domains, manage requests, and format the extracted data.

## Dependencies

The project uses the following dependency, managed via Maven:

- **Jsoup**: A Java library for working with real-world HTML. It provides a convenient API to extract and manipulate data using HTML5 DOM, CSS selectors, and jQuery-like syntax.
  - `groupId`: org.jsoup
  - `artifactId`: jsoup
  - `version`: 1.21.1

To install the dependency, simply run the following Maven command in the project's root directory:

```bash
mvn install
```

## Technical Details

The project is built around a producer-consumer model with multiple threads to optimize the scraping process.

### Multi-Threaded Architecture

The application uses a fixed thread pool to manage the main tasks:

- **URL Supplier**: One thread responsible for reading URLs from an input file.
- **HTTP Requester**: Multiple client threads that make asynchronous HTTP requests to fetch the HTML of the pages.
- **Content Filtering**: A dedicated thread processes the received HTML, extracting relevant content based on domain-specific filter strategies.
- **Formatting and Writing**: A final thread formats and saves the extracted content into text files.

### Control and Monitoring Mechanism

- **WebValveImpl**: Acts as a valve controlling the flow of URLs sent to the HTTP clients.
- **DynamicMonitor**: A crucial class that monitors and limits the number of active concurrent requests, helping to avoid being blocked for excessive requests. It can dynamically adjust the request limit based on network feedback (e.g., "too many concurrent streams" errors).

### Fetch Modes

The system can operate in different modes, configurable via command line:

- **AGGRESSIVE**: Retries failed requests a configurable number of times.
- **BALANCED**: Retries failed requests only during the second execution phase (error-handling phase).
- **ADAPTATIVE** (default): Dynamically adjusts the request rate based on network errors and HTTP status codes (like 429 - Too Many Requests).

### Domain-Specific Filter Strategy

The project uses the Strategy pattern to apply different HTML parsing logic for each site. Implementations of IHtmlFilter (WitchCultHtmlFilter, EminentHtmlFilter, ChickenHtmlFilter) are chosen dynamically based on the URL domain, making it easy to extend to new websites.

## How to Run

To run the project, compile it into a JAR file and execute it via the command line, passing the required arguments.

### Compile the project

Use Maven to create the executable JAR file:

```bash
mvn package
```

This will create a .jar file in the target/ directory.

### Prepare the URLs file

Create a text file (e.g., urls.txt) containing a list of URLs, one per line.

### Run via command line

```bash
java -jar target/untitled1-1.0-SNAPSHOT.jar [options] <path_to_urls_file>
```

**Available options**:
- `-r <number>`: Sets the total number of requests to be made (default: 100, max: 3000).
- `-m <mode>`: Sets the fetch mode. Options: AGGRESSIVE, BALANCED, ADAPTATIVE (default).
- `-t <number>`: Sets the number of retry attempts for failed requests (default: 3).

**Example execution**:

```bash
java -jar target/untitled1-1.0-SNAPSHOT.jar -r 500 -m ADAPTATIVE -t 5 urls.txt
```

## Project Output

After execution, the project will create the following files and directories in the working folder:

- **files/ directory**:  
  For each successfully processed URL, a .txt file will be created inside this directory.  
  The filename is derived from the last part of the URL path.  
  The content will be the extracted and formatted text from the corresponding web page.

- **urls2.txt**:  
  A log file containing all successfully processed URLs.

- **error.txt**:  
  A log file containing all URLs that failed after all retry attempts.

 # Important
## Local Spring Boot Server for Testing
This project is configured to run exclusively with a local Spring Boot server to ensure legal compliance. The URLs in the urls.txt file and the files used for testing, found in the repository at https://github.com/Playerrl-ux/mock-server/tree/master, are modified versions of those from the original websites. They contain random text to preserve potential copyrights. This setup allows for safe and controlled testing of the web scraping functionality without interacting with live websites.
